package io.github.alessandrojean.toshokan.domain

import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.core.util.Pair
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.Book
import io.github.alessandrojean.toshokan.database.data.BookGroup
import io.github.alessandrojean.toshokan.database.data.Person
import io.github.alessandrojean.toshokan.database.data.Publisher
import io.github.alessandrojean.toshokan.database.data.Store
import io.github.alessandrojean.toshokan.database.data.Tag
import io.github.alessandrojean.toshokan.util.extension.toLocalEpochMilli
import io.github.alessandrojean.toshokan.util.extension.toUtcEpochMilli
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.io.Serializable

sealed class SearchFilters {
  @Parcelize
  data class Complete(
    val query: String = "",
    val isFuture: Boolean? = false,
    val favoritesOnly: Boolean = false,
    val collections: List<Collection> = emptyList(),
    val groups: @RawValue List<BookGroup> = emptyList(),
    val tags: @RawValue List<Tag> = emptyList(),
    val publishers: @RawValue List<Publisher> = emptyList(),
    val contributors: @RawValue List<Person> = emptyList(),
    val stores: @RawValue List<Store> = emptyList(),
    val boughtAt: DateRange? = null,
    val readAt: DateRange? = null,
    val sortColumn: SortColumn = SortColumn.BOUGHT_AT,
    val sortDirection: SortDirection = SortDirection.DESCENDING
  ) : SearchFilters(), Parcelable, Serializable

  @Parcelize
  data class Incomplete(
    val query: String = "",
    val isFuture: Boolean? = false,
    val favoritesOnly: Boolean = false,
    val collections: List<String> = emptyList(),
    val groups: List<Long> = emptyList(),
    val tags: List<Long> = emptyList(),
    val publishers: List<Long> = emptyList(),
    val contributors: List<Long> = emptyList(),
    val stores: List<Long> = emptyList(),
    val boughtAt: DateRange? = null,
    val readAt: DateRange? = null,
    val sortColumn: SortColumn = SortColumn.BOUGHT_AT,
    val sortDirection: SortDirection = SortDirection.DESCENDING
  ) : SearchFilters(), Parcelable, Serializable
}

@Parcelize
data class DateRange(
  val start: Long,
  val end: Long
): Parcelable, Serializable {

  fun toSelection(): Pair<Long, Long> = Pair(start.toLocalEpochMilli(), end.toLocalEpochMilli())

  companion object {
    fun fromSelection(selection: Pair<Long, Long>) = DateRange(
      start = selection.first.toUtcEpochMilli(),
      end = selection.second.toUtcEpochMilli()
    )
  }

}

@Parcelize
data class Collection(
  val title: String,
  val count: Int = 0
) : Parcelable, Serializable {

  override fun equals(other: Any?): Boolean {
    if (other !is Collection) {
      return false
    }

    return other.title == title
  }

  override fun hashCode(): Int {
    var result = title.hashCode()
    result = 31 * result + count
    return result
  }
}

@Parcelize
enum class SortColumn(
  @StringRes val title: Int,
  val mapper: (Book) -> Comparable<*>
) : Parcelable, Serializable {
  TITLE(R.string.title, { it.title }),
  PAGE_COUNT(R.string.page_count, { it.page_count ?: 0 }),
  BOUGHT_AT(R.string.bought_at, { it.bought_at ?: 0L }),
  CREATED_AT(R.string.created_at, { it.created_at }),
  UPDATED_AT(R.string.updated_at, { it.updated_at })
}

@Parcelize
enum class SortDirection : Parcelable, Serializable {
  ASCENDING, DESCENDING;

  operator fun not(): SortDirection = if (this == ASCENDING) DESCENDING else ASCENDING
}