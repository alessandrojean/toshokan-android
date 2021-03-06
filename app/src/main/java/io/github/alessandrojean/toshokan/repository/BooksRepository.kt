package io.github.alessandrojean.toshokan.repository

import android.content.Context
import android.icu.util.Currency
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.squareup.sqldelight.android.paging3.QueryPagingSource
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.alessandrojean.toshokan.data.cache.CoverCache
import io.github.alessandrojean.toshokan.database.ToshokanDatabase
import io.github.alessandrojean.toshokan.database.data.Book
import io.github.alessandrojean.toshokan.database.data.BookContributor
import io.github.alessandrojean.toshokan.database.data.BookGroup
import io.github.alessandrojean.toshokan.database.data.CompleteBook
import io.github.alessandrojean.toshokan.database.data.PeriodStatistics
import io.github.alessandrojean.toshokan.database.data.Person
import io.github.alessandrojean.toshokan.database.data.Publisher
import io.github.alessandrojean.toshokan.database.data.Reading
import io.github.alessandrojean.toshokan.database.data.Statistics
import io.github.alessandrojean.toshokan.database.data.Store
import io.github.alessandrojean.toshokan.database.data.Tag
import io.github.alessandrojean.toshokan.domain.BookNeighbors
import io.github.alessandrojean.toshokan.domain.Collection
import io.github.alessandrojean.toshokan.domain.Contributor
import io.github.alessandrojean.toshokan.domain.DomainBook
import io.github.alessandrojean.toshokan.domain.DomainContributor
import io.github.alessandrojean.toshokan.domain.DomainDimensions
import io.github.alessandrojean.toshokan.domain.DomainPrice
import io.github.alessandrojean.toshokan.domain.DomainRelation
import io.github.alessandrojean.toshokan.domain.DomainTag
import io.github.alessandrojean.toshokan.domain.Price
import io.github.alessandrojean.toshokan.domain.RankingItem
import io.github.alessandrojean.toshokan.domain.RawTag
import io.github.alessandrojean.toshokan.domain.SearchFilters
import io.github.alessandrojean.toshokan.domain.SortColumn
import io.github.alessandrojean.toshokan.domain.SortDirection
import io.github.alessandrojean.toshokan.service.cover.BookCover
import io.github.alessandrojean.toshokan.util.extension.currentTime
import io.github.alessandrojean.toshokan.util.extension.firstDay
import io.github.alessandrojean.toshokan.util.extension.lastDay
import io.github.alessandrojean.toshokan.util.extension.lastDayOfCurrentMonth
import io.github.alessandrojean.toshokan.util.extension.toTitleParts
import io.github.alessandrojean.toshokan.util.extension.toZonedDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import logcat.LogPriority
import logcat.logcat
import java.text.Collator
import java.time.Month
import java.time.Year
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BooksRepository @Inject constructor(
  @ApplicationContext private val context: Context,
  private val database: ToshokanDatabase,
  private val publishersRepository: PublishersRepository,
  private val groupsRepository: GroupsRepository,
  private val storesRepository: StoresRepository,
  private val peopleRepository: PeopleRepository,
  private val tagsRepository: TagsRepository,
  private val coverCache: CoverCache
) {

  fun findById(id: Long): CompleteBook? {
    return database.bookQueries.completeBook(id).executeAsOneOrNull()
  }

  fun findAllCodes(): List<String> {
    return database.bookQueries.findAllCodes().executeAsList()
  }

  fun findByIdAsFlow(id: Long): Flow<DomainBook?> {
    return database.bookQueries.completeBook(id).asFlow().mapToOneOrNull()
      .map { bookDb ->
        if (bookDb == null) {
          return@map null
        }

        DomainBook(
          id = bookDb.id,
          code = bookDb.code,
          group = DomainRelation(bookDb.group_id, bookDb.group_name),
          title = bookDb.title,
          contributors = emptyList(),
          publisher = DomainRelation(bookDb.publisher_id, bookDb.publisher_name),
          dimensions = DomainDimensions(bookDb.dimension_width, bookDb.dimension_height),
          isFuture = bookDb.is_future,
          labelPrice = DomainPrice(
            currency = bookDb.label_price_currency.currencyCode,
            value = bookDb.label_price_value
          ),
          paidPrice = DomainPrice(
            currency = bookDb.paid_price_currency.currencyCode,
            value = bookDb.paid_price_value
          ),
          store = DomainRelation(bookDb.store_id, bookDb.store_name),
          coverUrl = bookDb.cover_url,
          boughtAt = bookDb.bought_at,
          isFavorite = bookDb.is_favorite ?: false,
          synopsis = bookDb.synopsis,
          notes = bookDb.notes,
          tags = emptyList(),
          readingCount = bookDb.reading_count,
          latestReading = bookDb.latest_reading,
          pageCount = bookDb.page_count,
          createdAt = bookDb.created_at,
          updatedAt = bookDb.updated_at,
        )
      }
      .flowOn(Dispatchers.IO)
  }

  fun findSimpleById(id: Long): Flow<Book?> {
    return database.bookQueries.findById(id).asFlow().mapToOneOrNull().flowOn(Dispatchers.IO)
  }

  fun findByCode(code: String): Book? {
    return database.bookQueries.findByCode(code).executeAsOneOrNull()
  }

  fun findBookContributorsAsFlow(id: Long): Flow<List<DomainContributor>> {
    return database.bookCreditQueries.bookContributor(id).asFlow().mapToList()
      .map { contributorsDb ->
        contributorsDb.map { contributorDb ->
          DomainContributor(
            id = contributorDb.id,
            personId = contributorDb.person_id,
            name = contributorDb.person_name,
            role = contributorDb.role
          )
        }
      }
      .flowOn(Dispatchers.IO)
  }

  fun findBookContributors(id: Long): List<BookContributor> {
    return database.bookCreditQueries.bookContributor(id).executeAsList()
  }

  fun findBookTags(id: Long): List<Tag> {
    return database.bookTagQueries.findByBookId(id).executeAsList()
  }

  fun findBookTagsAsFlow(id: Long): Flow<List<DomainTag>> {
    return database.bookTagQueries.findByBookId(id).asFlow()
      .mapToList()
      .map { tagsDb ->
        tagsDb.map { tagDb ->
          DomainTag(
            id = tagDb.id,
            title = tagDb.name,
            isNsfw = tagDb.is_nsfw
          )
        }
      }
      .flowOn(Dispatchers.IO)
  }

  fun findReadings(id: Long, descending: Boolean = true): Flow<List<Reading>> {
    return database.readingQueries.findByBook(id).asFlow().mapToList()
      .map { if (!descending) it.reversed() else it }
      .flowOn(Dispatchers.IO)
  }

  fun subscribeToCollections(): Flow<List<Collection>> {
    return database.bookQueries.allTitles().asFlow().mapToList()
      .map { allTitles ->
        allTitles.groupBy { it.title.toTitleParts().title }
          .flatMap { (collectionName, volumes) ->
            volumes
              .groupBy { it.group_id }
              .map { (groupId, volumesByGroup) ->
                Collection(
                  title = collectionName,
                  count = volumesByGroup.size,
                  groupId = groupId,
                  groupName = volumesByGroup[0].group_name
                )
              }
          }
      }
      .flowOn(Dispatchers.IO)
  }

  fun subscribeToCollectionsRanking(limit: Int = 20): Flow<List<RankingItem>> {
    return subscribeToCollections().map { collections ->
      collections.sortedByDescending { it.count }
        .take(limit)
        .map { collection ->
          RankingItem(
            itemId = collection.groupId,
            title = collection.title,
            count = collection.count.toLong(),
            extra = collection.groupName
          )
        }
    }
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

  fun subscribeToStatistics(currency: Currency): Flow<Statistics> {
    return database.bookQueries.statistics(currency).asFlow().mapToOne().flowOn(Dispatchers.IO)
  }

  fun subscribeToPeriodStatistics(currency: Currency, start: Long, end: Long): Flow<PeriodStatistics> {
    return database.bookQueries
      .periodStatistics(
        currency = currency,
        startPeriod = start,
        endPeriod = end
      )
      .asFlow()
      .mapToOne()
      .flowOn(Dispatchers.IO)
  }

  fun subscribeToMonthlyExpense(currency: Currency, year: Year): Flow<Map<Month, Float>> {
    return database.bookQueries
      .monthlyExpense(
        currency = currency,
        startPeriod = year.firstDay,
        endPeriod = if (year.value == Year.now().value) lastDayOfCurrentMonth else year.lastDay
      )
      .asFlow()
      .mapToList()
      .map { entries ->
        entries
          .map { it.bought_at.toZonedDateTime() to it.paid_price_value }
          .groupBy({ it.first.month }) { it.second }
          .mapValues { (_, values) -> values.sum() }
      }
      .flowOn(Dispatchers.IO)
  }

  fun subscribeToMonthlyBoughts(year: Year): Flow<Map<Month, Int>> {
    return database.bookQueries
      .monthlyBoughts(
        startPeriod = year.firstDay,
        endPeriod = if (year.value == Year.now().value) lastDayOfCurrentMonth else year.lastDay
      )
      .asFlow()
      .mapToList()
      .map { entries ->
        entries
          .map { it.toZonedDateTime() }
          .groupBy { it.month }
          .mapValues { (_, values) -> values.size }
      }
      .flowOn(Dispatchers.IO)
  }

  fun subscribeToMonthlyReads(year: Year): Flow<Map<Month, Int>> {
    return database.bookQueries
      .monthlyReads(
        startPeriod = year.firstDay,
        endPeriod = if (year.value == Year.now().value) lastDayOfCurrentMonth else year.lastDay
      )
      .asFlow()
      .mapToList()
      .map { entries ->
        entries
          .map { it.toZonedDateTime() }
          .groupBy { it.month }
          .mapValues { (_, values) -> values.size }
      }
      .flowOn(Dispatchers.IO)
  }

  fun findSeriesVolumes(book: CompleteBook): Flow<BookNeighbors?> {
    val title = book.title.toTitleParts()

    return database.bookQueries
      .seriesVolumes("${title.title} #", book.publisher_id, book.group_id)
      .asFlow()
      .mapToList()
      .map { collection ->
        if (collection.size <= 1) {
          return@map null
        }

        val bookIndex = collection.indexOfFirst { it.id == book.id }

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
        tagIds = filters.tags.map(Tag::id),
        tagsIsEmpty = filters.tags.isEmpty(),
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
        val collections = filters.collections

        var results = if (collections.isNotEmpty()) {
          val allTitles = collections.map { it.title }
          val allGroupIds = collections.mapNotNull { it.groupId }

          searchResults
            .filter { it.title.toTitleParts().title in allTitles }
            .filter { allGroupIds.isEmpty() || it.group_id in allGroupIds }
        } else {
          searchResults
        }

        results = if (filters.sortColumn == SortColumn.TITLE) {
          val collator = Collator.getInstance(Locale.getDefault())
          results.sortedWith(compareBy(collator, filters.sortColumn.mapper))
        } else {
          results.sortedWith(compareBy(filters.sortColumn.mapper))
        }

        if (!filters.sortDirection == SortDirection.DESCENDING) {
          results.asReversed()
        } else {
          results
        }
      }
      .flowOn(Dispatchers.IO)
  }

  suspend fun insertDomain(book: DomainBook): Long? = withContext(Dispatchers.IO) {
    val publisherId = publishersRepository.findByName(book.publisher.title!!)?.id
      ?: publishersRepository.insert(name = book.publisher.title)
    val groupId = groupsRepository.findByName(book.group.title!!)?.id
      ?: groupsRepository.insert(book.group.title)
    val storeId = storesRepository.findByName(book.store.title!!)?.id
      ?: storesRepository.insert(book.store.title)

    val contributors = book.contributors.map { contributor ->
      if (contributor.id != null) {
        Contributor(personId = contributor.id, role = contributor.role)
      } else {
        Contributor(
          personId = peopleRepository.findByName(contributor.name!!)?.id
            ?: peopleRepository.insert(contributor.name),
          role = contributor.role
        )
      }
    }

    val tags = book.tags.map { tag ->
      if (tag.id != null) {
        tag.toRawTag()
      } else {
        RawTag(
          tagId = tagsRepository.findByName(tag.title!!)?.id
            ?: tagsRepository.insert(tag.title)
        )
      }
    }

    insert(
      code = book.code,
      title = book.title,
      volume = book.title.toTitleParts().number,
      synopsis = book.synopsis,
      notes = book.notes,
      publisherId = publisherId!!,
      groupId = groupId!!,
      paidPrice = book.paidPrice.toPrice(),
      labelPrice = book.labelPrice.toPrice(),
      storeId = storeId!!,
      boughtAt = book.boughtAt,
      isFuture = book.isFuture,
      pageCount = book.pageCount ?: 0,
      cover = book.coverUrl?.let { BookCover.External(it) },
      dimensionWidth = book.dimensions.width,
      dimensionHeight = book.dimensions.height,
      contributors = contributors,
      tags = tags
    )
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
    pageCount: Int = 0,
    cover: BookCover?,
    dimensionWidth: Float,
    dimensionHeight: Float,
    contributors: List<Contributor>,
    tags: List<RawTag> = emptyList()
  ): Long? = withContext(Dispatchers.IO) {
    val now = currentTime
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
        page_count = pageCount,
        cover_url = (cover as? BookCover.External)?.imageUrl,
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

      tags.forEach { rawTag ->
        database.bookTagQueries.insert(bookId!!, rawTag.tag?.id ?: rawTag.tagId!!)
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
    pageCount: Int = 0,
    cover: BookCover?,
    dimensionWidth: Float,
    dimensionHeight: Float,
    contributors: List<Contributor>,
    tags: List<RawTag> = emptyList()
  ) = withContext(Dispatchers.IO) {
    val now = currentTime

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
        page_count = pageCount,
        cover_url = (cover as? BookCover.External)?.imageUrl,
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

      database.bookTagQueries.deleteBulk(id)

      tags.forEach { rawTag ->
        database.bookTagQueries.insert(id, rawTag.tag?.id ?: rawTag.tagId!!)
      }
    }

    val customCoverFile = coverCache.getCustomCoverFile(id.toString())

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
    if (coverCache.getCustomCoverFile(id.toString()).exists()) {
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