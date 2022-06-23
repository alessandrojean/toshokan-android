package io.github.alessandrojean.toshokan.repository

import android.content.Context
import android.icu.util.Currency
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.squareup.sqldelight.android.paging3.QueryPagingSource
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.alessandrojean.toshokan.data.backup.models.ToshokanSheetBook
import io.github.alessandrojean.toshokan.data.backup.models.ToshokanSheetStatus
import io.github.alessandrojean.toshokan.data.cache.CoverCache
import io.github.alessandrojean.toshokan.database.ToshokanDatabase
import io.github.alessandrojean.toshokan.database.data.Book
import io.github.alessandrojean.toshokan.database.data.BookContributor
import io.github.alessandrojean.toshokan.database.data.BookGroup
import io.github.alessandrojean.toshokan.database.data.CompleteBook
import io.github.alessandrojean.toshokan.database.data.Person
import io.github.alessandrojean.toshokan.database.data.Publisher
import io.github.alessandrojean.toshokan.database.data.Reading
import io.github.alessandrojean.toshokan.database.data.Store
import io.github.alessandrojean.toshokan.domain.BookNeighbors
import io.github.alessandrojean.toshokan.domain.Contributor
import io.github.alessandrojean.toshokan.domain.CreditRole
import io.github.alessandrojean.toshokan.domain.Library
import io.github.alessandrojean.toshokan.domain.LibraryGroup
import io.github.alessandrojean.toshokan.domain.Price
import io.github.alessandrojean.toshokan.domain.SearchFilters
import io.github.alessandrojean.toshokan.service.cover.BookCover
import io.github.alessandrojean.toshokan.util.extension.TitleParts
import io.github.alessandrojean.toshokan.util.extension.toSheetDate
import io.github.alessandrojean.toshokan.util.extension.toTitleParts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import logcat.LogPriority
import logcat.logcat
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BooksRepository @Inject constructor(
  @ApplicationContext private val context: Context,
  private val database: ToshokanDatabase,
  private val coverCache: CoverCache
) {

  fun findById(id: Long): CompleteBook? {
    return database.bookQueries.completeBook(id).executeAsOneOrNull()
  }

  fun findAllCodes(): List<String> {
    return database.bookQueries.findAllCodes().executeAsList()
  }

  fun findByIdAsFlow(id: Long): Flow<CompleteBook?> {
    return database.bookQueries.completeBook(id).asFlow().mapToOneOrNull().flowOn(Dispatchers.IO)
  }

  fun findSimpleById(id: Long): Flow<Book?> {
    return database.bookQueries.findById(id).asFlow().mapToOneOrNull().flowOn(Dispatchers.IO)
  }

  fun findByCode(code: String): Book? {
    return database.bookQueries.findByCode(code).executeAsOneOrNull()
  }

  fun findBookContributorsAsFlow(id: Long): Flow<List<BookContributor>> {
    return database.bookCreditQueries.bookContributor(id).asFlow().mapToList().flowOn(Dispatchers.IO)
  }

  fun findBookContributors(id: Long): List<BookContributor> {
    return database.bookCreditQueries.bookContributor(id).executeAsList()
  }

  fun findReadings(id: Long, descending: Boolean = true): Flow<List<Reading>> {
    return database.readingQueries.findByBook(id).asFlow().mapToList()
      .map { if (!descending) it.reversed() else it }
      .flowOn(Dispatchers.IO)
  }

  fun findCollections(): Flow<List<String>> {
    return database.bookQueries.allTitles().asFlow().mapToList()
      .map { allTitles ->
        allTitles.groupBy { it.toTitleParts().title }
          .filterValues { it.size > 1 }
          .keys.toList()
      }
      .flowOn(Dispatchers.IO)
  }

  fun groupBooksPaginated(groupId: Long, isFuture: Boolean = false): Flow<PagingData<Book>> {
    val pager = Pager(config = PagingConfig(pageSize = PAGE_SIZE)) {
      QueryPagingSource(
        countQuery = database.bookQueries.countGroupBooks(groupId, isFuture),
        transacter = database.bookQueries,
        dispatcher = Dispatchers.IO,
        queryProvider = { limit, offset -> database.bookQueries.groupBooks(groupId, isFuture, limit, offset) }
      )
    }

    return pager.flow.flowOn(Dispatchers.IO)
  }

  fun findLibraryBooks(isFuture: Boolean = false): Flow<Library> {
    return database.bookQueries.libraryItems(is_future = isFuture).asFlow().mapToList()
      .map { libraryItems ->
        val groups = libraryItems.groupBy { it.group_id }
          .map { (groupId, libraryItems) ->
            val group = LibraryGroup(
              id = groupId,
              name = libraryItems[0].group_name,
              sort = libraryItems[0].group_sort
            )

            val books = libraryItems.map { item ->
              Book(
                id = item.id,
                code = item.code,
                title = item.title,
                volume = item.volume,
                synopsis = item.synopsis,
                notes = item.notes,
                publisher_id = item.publisher_id,
                group_id = item.group_id,
                paid_price_currency = item.paid_price_currency,
                paid_price_value = item.paid_price_value,
                label_price_currency = item.label_price_currency,
                label_price_value = item.label_price_value,
                store_id = item.store_id,
                bought_at = item.bought_at,
                is_future = item.is_future,
                cover_url = item.cover_url,
                dimension_width = item.dimension_width,
                dimension_height = item.dimension_height,
                created_at = item.created_at,
                updated_at = item.updated_at,
                is_favorite = item.is_favorite,
              )
            }

            group to books
          }

        Library(groups = groups.toMap())
      }
      .flowOn(Dispatchers.IO)
  }

  fun findSeriesVolumes(title: TitleParts, publisherId: Long, groupId: Long): Flow<BookNeighbors?> {
    return database.bookQueries.seriesVolumes("${title.title} #", publisherId, groupId)
      .asFlow()
      .mapToList()
      .map { collection ->
        if (collection.size <= 1) {
          return@map null
        }

        val bookIndex = collection.indexOfFirst { it.title == title.full }

        BookNeighbors(
          first = collection.firstOrNull(),
          previous = collection.getOrNull(bookIndex - 1),
          current = collection.getOrNull(bookIndex),
          next = collection.getOrNull(bookIndex + 1),
          last = collection.lastOrNull(),
          count = collection.size
        )
      }
      .flowOn(Dispatchers.IO)
  }

  fun search(filters: SearchFilters.Complete): Flow<List<Book>> {
    return database.bookQueries
      .search(
        query = filters.query.ifBlank { null },
        isFuture = filters.isFuture,
        isFavorite = if (filters.favoritesOnly) true else null,
        groupIds = filters.groups.map(BookGroup::id),
        groupsIsEmpty = filters.groups.isEmpty(),
        publisherIds = filters.publishers.map(Publisher::id),
        publishersIsEmpty = filters.publishers.isEmpty(),
        storeIds = filters.stores.map(Store::id),
        storesIsEmpty = filters.stores.isEmpty(),
        boughtAtStart = filters.boughtAt?.start,
        boughtAtEnd = filters.boughtAt?.end,
        readAtStart = filters.readAt?.start,
        readAtEnd = filters.readAt?.end,
        contributorsIsEmpty = filters.contributors.isEmpty(),
        contributors = filters.contributors.map(Person::id)
      )
      .asFlow()
      .mapToList()
      .map { searchResults ->
        if (filters.collections.isNotEmpty()) {
          searchResults.filter { it.title.toTitleParts().title in filters.collections }
        } else {
          searchResults
        }
      }
      .flowOn(Dispatchers.IO)
  }

  suspend fun insert(
    code: String?,
    title: String,
    volume: String?,
    synopsis: String?,
    notes: String?,
    publisherId: Long,
    groupId: Long,
    paidPrice: Price,
    labelPrice: Price,
    storeId: Long,
    boughtAt: Long?,
    isFuture: Boolean = false,
    cover: BookCover?,
    dimensionWidth: Float,
    dimensionHeight: Float,
    contributors: List<Contributor>
  ): Long? = withContext(Dispatchers.IO) {
    val now = Date().time
    var bookId: Long? = null

    database.transaction {
      database.bookQueries.insert(
        code = code,
        title = title,
        volume = volume,
        synopsis = synopsis,
        notes = notes,
        publisher_id = publisherId,
        group_id = groupId,
        paid_price_currency = paidPrice.currency,
        paid_price_value = paidPrice.value,
        label_price_currency = labelPrice.currency,
        label_price_value = labelPrice.value,
        store_id = storeId,
        bought_at = boughtAt,
        is_future = isFuture,
        cover_url = if (cover is BookCover.External) {
          cover.imageUrl
        } else {
          null
        },
        dimension_width = dimensionWidth,
        dimension_height = dimensionHeight,
        created_at = now,
        updated_at = now
      )

      bookId = database.bookQueries.lastInsertedId().executeAsOne().max!!

      contributors.forEach { contributor ->
        database.bookCreditQueries.insert(
          book_id = bookId!!,
          person_id = contributor.person?.id ?: contributor.personId!!,
          role = contributor.role
        )
      }
    }

    if (cover is BookCover.Custom) {
      val customCoverResult = runCatching {
        context.contentResolver.openInputStream(cover.uri)?.use { inputStream ->
          coverCache.setCustomCoverToCache(
            book = database.bookQueries.findById(bookId!!).executeAsOne(),
            inputStream = inputStream
          )
        }
      }

      customCoverResult.exceptionOrNull()?.let {
        logcat(LogPriority.ERROR) { "Failed to save the custom cover for the book $bookId" }
      }
    }

    bookId
  }

  suspend fun update(
    id: Long,
    code: String?,
    title: String,
    volume: String?,
    synopsis: String?,
    notes: String?,
    publisherId: Long,
    groupId: Long,
    paidPrice: Price,
    labelPrice: Price,
    storeId: Long,
    boughtAt: Long?,
    isFuture: Boolean = false,
    cover: BookCover?,
    dimensionWidth: Float,
    dimensionHeight: Float,
    contributors: List<Contributor>
  ) = withContext(Dispatchers.IO) {
    val now = Date().time

    database.transaction {
      database.bookQueries.update(
        id = id,
        code = code,
        title = title,
        volume = volume,
        synopsis = synopsis,
        notes = notes,
        publisher_id = publisherId,
        group_id = groupId,
        paid_price_currency = paidPrice.currency,
        paid_price_value = paidPrice.value,
        label_price_currency = labelPrice.currency,
        label_price_value = labelPrice.value,
        store_id = storeId,
        bought_at = boughtAt,
        is_future = isFuture,
        cover_url = if (cover is BookCover.External) {
          cover.imageUrl
        } else {
          null
        },
        dimension_width = dimensionWidth,
        dimension_height = dimensionHeight,
        updated_at = now
      )

      database.bookCreditQueries.deleteBulk(id)

      contributors.forEach { contributor ->
        database.bookCreditQueries.insert(
          book_id = id,
          person_id = contributor.person?.id ?: contributor.personId!!,
          role = contributor.role
        )
      }
    }

    val customCoverFile = coverCache.getCustomCoverFile(id)

    val coverResult = runCatching {
      if (customCoverFile.exists() && cover !is BookCover.Custom) {
        coverCache.deleteCustomCover(id)
      } else if (cover is BookCover.Custom) {
        context.contentResolver.openInputStream(cover.uri)?.use { inputStream ->
          coverCache.deleteCustomCover(id)
          coverCache.setCustomCoverToCache(
            bookId = id,
            inputStream = inputStream
          )
        }
      } else { /* Do nothing */ }
    }

    coverResult.exceptionOrNull()?.let {
      logcat(LogPriority.ERROR) { "Failed to update the custom cover for book $id" }
    }
  }

  suspend fun deleteCover(id: Long, coverUrl: String?) = withContext(Dispatchers.IO) {
    if (coverCache.getCustomCoverFile(id).exists()) {
      coverCache.deleteCustomCover(id)
    } else {
      database.bookQueries.clearCoverUrl(id)
      coverCache.deleteFromCache(id, coverUrl, true)
    }
  }

  suspend fun toggleFavorite(id: Long) = withContext(Dispatchers.IO) {
    database.bookQueries.toggleFavorite(id = id, updated_at = Date().time)
  }

  suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
    val book = database.bookQueries.findById(id).executeAsOne()

    runCatching {
      coverCache.deleteFromCache(book, deleteCustomCover = true)
    }

    database.bookQueries.delete(id)
  }

  suspend fun delete(ids: List<Long>) = withContext(Dispatchers.IO) {
    runCatching {
      database.bookQueries.findByIds(ids).executeAsList().forEach { book ->
        coverCache.deleteFromCache(book, deleteCustomCover = true)
      }
    }

    database.bookQueries.deleteBulk(ids)
  }

  suspend fun insertReading(bookId: Long, readAt: Long?) = withContext(Dispatchers.IO) {
    database.readingQueries.insert(bookId, readAt)
  }

  suspend fun bulkDeleteReadings(ids: List<Long>) = withContext(Dispatchers.IO) {
    database.readingQueries.deleteBulk(ids)
  }

  companion object {
    private const val PAGE_SIZE = 20
  }

}