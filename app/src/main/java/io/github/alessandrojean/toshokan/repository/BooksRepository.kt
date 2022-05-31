package io.github.alessandrojean.toshokan.repository

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import io.github.alessandrojean.toshokan.database.ToshokanDatabase
import io.github.alessandrojean.toshokan.database.data.Book
import io.github.alessandrojean.toshokan.domain.Contributor
import io.github.alessandrojean.toshokan.domain.Price
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BooksRepository @Inject constructor(
  private val database: ToshokanDatabase
) {

  fun findById(id: Long): Flow<Book> {
    return database.bookQueries.findById(id).asFlow().mapToOne()
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

}