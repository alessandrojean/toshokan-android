package io.github.alessandrojean.toshokan.data.backup

/**
 * Protobuf backup restorer.
 *
 * Based on Tachiyomi's implementation.
 * https://github.com/tachiyomiorg/tachiyomi
 *
 * Copyright 2015 Javier Tomás
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */

import android.content.Context
import android.net.Uri
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.data.backup.models.ToshokanSheet
import io.github.alessandrojean.toshokan.data.backup.models.ToshokanSheetBook
import io.github.alessandrojean.toshokan.data.backup.models.ToshokanSheetStatus
import io.github.alessandrojean.toshokan.domain.Contributor
import io.github.alessandrojean.toshokan.domain.CreditRole
import io.github.alessandrojean.toshokan.domain.RawTag
import io.github.alessandrojean.toshokan.repository.BooksRepository
import io.github.alessandrojean.toshokan.repository.GroupsRepository
import io.github.alessandrojean.toshokan.repository.PeopleRepository
import io.github.alessandrojean.toshokan.repository.PublishersRepository
import io.github.alessandrojean.toshokan.repository.StoresRepository
import io.github.alessandrojean.toshokan.repository.TagsRepository
import io.github.alessandrojean.toshokan.service.cover.BookCover
import io.github.alessandrojean.toshokan.util.extension.toUtcEpochMilliFromSheet
import io.github.alessandrojean.toshokan.util.extension.toTitleParts
import io.github.alessandrojean.toshokan.util.removeDashes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import logcat.LogPriority
import logcat.logcat
import okio.buffer
import okio.gzip
import okio.source

class SheetBackupRestorer @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted notifier: BackupNotifier,
  private val booksRepository: BooksRepository,
  private val groupsRepository: GroupsRepository,
  private val publishersRepository: PublishersRepository,
  private val storesRepository: StoresRepository,
  private val peopleRepository: PeopleRepository,
  private val tagsRepository: TagsRepository
) : AbstractBackupRestorer(context, notifier) {

  @AssistedFactory
  interface Factory {
    fun create(@Assisted context: Context, @Assisted notifier: BackupNotifier): SheetBackupRestorer
  }

  companion object {
    const val BACKUP_EXTENSION = "proto.gz"
    const val BACKUP_MIME = "application/gzip"
  }

  private val parser = ProtoBuf

  @Throws(SheetRestoreException::class)
  override suspend fun performRestore(uri: Uri): Boolean = withContext(Dispatchers.IO) {
    val result = runCatching {
      if (context.contentResolver.getType(uri) != BACKUP_MIME) {
        return@withContext false
      }

      val backupGunzipped = context.contentResolver.openInputStream(uri)!!.source().gzip().buffer()
        .use { it.readByteArray() }
      val backup = parser.decodeFromByteArray<ToshokanSheet>(backupGunzipped)

      if (backup.library.isEmpty()) {
        throw SheetRestoreException(context.getString(R.string.error_backup_no_books))
      }

      val existingBooks = booksRepository.findAllCodes()
      // Backup only adds if it doesn't exist.
      // TODO: Handle when there's more than one book with the same code, but different volumes.
      val booksToInsert = backup.library.filter { it.code.trim().removeDashes() !in existingBooks }

      if (booksToInsert.isEmpty()) {
        throw SheetRestoreException(context.getString(R.string.error_backup_no_books_to_restore))
      }

      restoreAmount = booksToInsert.size + 5

      val groupsMap = restoreGroups(backup.groups.filterNot(String::isBlank))
      val publishersMap = restorePublishers(backup.publishers.filterNot(String::isBlank))
      val storesMap = restoreStores(backup.stores.filterNot(String::isBlank))
      val peopleMap = restorePeople(backup.authors.filterNot(String::isBlank))
      val tagsMap = restoreTags(backup.tags.filterNot(String::isBlank))

      restoreBooks(booksToInsert, groupsMap, publishersMap, storesMap, peopleMap, tagsMap)
    }

    result.exceptionOrNull()?.let {
      logcat(LogPriority.ERROR) { it.stackTraceToString() }
      throw it
    }

    result.isSuccess
  }

  private suspend fun restoreBooks(
    books: List<ToshokanSheetBook>,
    groupsMap: Map<String, Long>,
    publishersMap: Map<String, Long>,
    storesMap: Map<String, Long>,
    peopleMap: Map<String, Long>,
    tagsMap: Map<String, Long>
  ) {
    books.forEach { sheetBook ->
      if (job?.isActive != true) {
        return
      }

      val bookId = booksRepository.insert(
        code = sheetBook.code.trim().removeDashes(),
        title = sheetBook.title,
        volume = sheetBook.title.toTitleParts().number,
        synopsis = sheetBook.synopsis,
        notes = sheetBook.notes,
        publisherId = publishersMap[sheetBook.publisher]!!,
        groupId = groupsMap[sheetBook.group]!!,
        paidPrice = sheetBook.paidPrice.toPrice(),
        labelPrice = sheetBook.labelPrice.toPrice(),
        storeId = storesMap[sheetBook.store]!!,
        boughtAt = sheetBook.boughtAt?.toUtcEpochMilliFromSheet(),
        isFuture = sheetBook.status == ToshokanSheetStatus.FUTURE,
        cover = sheetBook.coverUrl?.let { BookCover.External(it) },
        dimensionWidth = sheetBook.dimensions.width,
        dimensionHeight = sheetBook.dimensions.height,
        contributors = sheetBook.authors.mapNotNull { author ->
          peopleMap[author]?.let { personId ->
            Contributor(personId = personId, role = CreditRole.AUTHOR)
          }
        },
        tags = sheetBook.tags
          .filterNot(String::isEmpty)
          .map { tag -> RawTag(tagId = tagsMap[tag]!!) }
      )

      if (sheetBook.status == ToshokanSheetStatus.READ) {
        booksRepository.insertReading(bookId!!, sheetBook.readAt?.toUtcEpochMilliFromSheet())
      }

      restoreProgress++
      showRestoreProgress(restoreProgress, restoreAmount, sheetBook.title)
    }
  }

  private suspend fun restoreGroups(groups: List<String>): Map<String, Long> {
    val existingGroups = groupsRepository.findAll()

    val map = groups.associateWith { sheetGroup ->
      val dbGroup = existingGroups.firstOrNull { it.name.equals(sheetGroup, ignoreCase = true) }
      val dbGroupId = dbGroup?.id ?: groupsRepository.insert(sheetGroup)!!

      dbGroupId
    }

    restoreProgress++
    showRestoreProgress(restoreProgress, restoreAmount, context.getString(R.string.groups))

    return map
  }

  private suspend fun restoreTags(tags: List<String>): Map<String, Long> {
    val existingTags = tagsRepository.findAll()

    val map = tags.associateWith { sheetTag ->
      val dbTag = existingTags.firstOrNull { it.name.equals(sheetTag, ignoreCase = true) }
      val dbTagId = dbTag?.id ?: tagsRepository.insert(sheetTag)!!

      dbTagId
    }

    restoreProgress++
    showRestoreProgress(restoreProgress, restoreAmount, context.getString(R.string.tags))

    return map
  }

  private suspend fun restorePublishers(publishers: List<String>): Map<String, Long> {
    val existingPublishers = publishersRepository.selectAll()

    val map = publishers.associateWith { sheetPublisher ->
      val dbPublisher = existingPublishers.firstOrNull { it.name.equals(sheetPublisher, ignoreCase = true) }
      val dbPublisherId = dbPublisher?.id ?: publishersRepository.insert(sheetPublisher)!!

      dbPublisherId
    }

    restoreProgress++
    showRestoreProgress(restoreProgress, restoreAmount, context.getString(R.string.publishers))

    return map
  }

  private suspend fun restoreStores(stores: List<String>): Map<String, Long> {
    val existingStores = storesRepository.selectAll()

    val map = stores.associateWith { sheetStore ->
      val dbStore = existingStores.firstOrNull { it.name.equals(sheetStore, ignoreCase = true) }
      val dbStoreId = dbStore?.id ?: storesRepository.insert(sheetStore)!!

      dbStoreId
    }

    restoreProgress++
    showRestoreProgress(restoreProgress, restoreAmount, context.getString(R.string.stores))

    return map
  }

  private suspend fun restorePeople(people: List<String>): Map<String, Long> {
    val existingPeople = peopleRepository.selectAll()

    val map = people.associateWith { sheetPerson ->
      val dbPerson = existingPeople.firstOrNull { it.name.equals(sheetPerson, ignoreCase = true) }
      val dbPersonId = dbPerson?.id ?: peopleRepository.insert(sheetPerson)!!

      dbPersonId
    }

    restoreProgress++
    showRestoreProgress(restoreProgress, restoreAmount, context.getString(R.string.people))

    return map
  }

}