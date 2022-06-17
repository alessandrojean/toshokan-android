package io.github.alessandrojean.toshokan.repository

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
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
import io.github.alessandrojean.toshokan.domain.Library
import io.github.alessandrojean.toshokan.domain.LibraryBook
import io.github.alessandrojean.toshokan.domain.LibraryGroup
import io.github.alessandrojean.toshokan.domain.Price
import io.github.alessandrojean.toshokan.domain.SearchFilters
import io.github.alessandrojean.toshokan.util.extension.TitleParts
import io.github.alessandrojean.toshokan.util.extension.toTitleParts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BooksRepository @Inject constructor(
  private val database: ToshokanDatabase
) {

  fun findById(id: Long): Flow<CompleteBook?> {
    return database.bookQueries.completeBook(id).asFlow().mapToOneOrNull()
  }

  fun findByCode(code: String): Book? {
    return database.bookQueries.findByCode(code).executeAsOneOrNull()
  }

  fun findBookContributorsAsFlow(id: Long): Flow<List<BookContributor>> {
    return database.bookCreditQueries.bookContributor(id).asFlow().mapToList()
  }

  fun findBookContributors(id: Long): List<BookContributor> {
    return database.bookCreditQueries.bookContributor(id).executeAsList()
  }

  fun findReadings(id: Long, descending: Boolean = true): Flow<List<Reading>> {
    return database.readingQueries.findByBook(id).asFlow().mapToList()
      .map { if (!descending) it.reversed() else it }
  }

  fun findCollections(): Flow<List<String>> {
    return database.bookQueries.allTitles().asFlow().mapToList()
      .map { allTitles ->
        allTitles.groupBy { it.toTitleParts().title }
          .filterValues { it.size > 1 }
          .keys.toList()
      }
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
              LibraryBook(
                id = item.id,
                title = item.title,
                volume = item.volume,
                coverUrl = item.cover_url,
                isFuture = item.is_future
              )
            }

            group to books
          }

        Library(groups = groups.toMap())
      }
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
  }

  suspend fun search(filters: SearchFilters.Complete): List<Book> = withContext(Dispatchers.IO) {
    var results = database.bookQueries
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
      .executeAsList()

    if (filters.collections.isNotEmpty()) {
      results = results.filter { it.title.toTitleParts().title in filters.collections }
    }

    results
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
    coverUrl: String?,
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
        cover_url = coverUrl,
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
    coverUrl: String?,
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
        cover_url = coverUrl,
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
  }

  suspend fun toggleFavorite(id: Long) = withContext(Dispatchers.IO) {
    database.bookQueries.toggleFavorite(id = id, updated_at = Date().time)
  }

  suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
    database.bookQueries.delete(id)
  }

  suspend fun delete(ids: List<Long>) = withContext(Dispatchers.IO) {
    database.bookQueries.deleteBulk(ids)
  }

  suspend fun insertReading(bookId: Long, readAt: Long?) = withContext(Dispatchers.IO) {
    database.readingQueries.insert(bookId, readAt)
  }

  suspend fun bulkDeleteReadings(ids: List<Long>) = withContext(Dispatchers.IO) {
    database.readingQueries.deleteBulk(ids)
  }

}