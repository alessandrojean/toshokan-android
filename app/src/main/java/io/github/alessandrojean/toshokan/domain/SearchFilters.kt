package io.github.alessandrojean.toshokan.domain

import androidx.core.util.Pair
import io.github.alessandrojean.toshokan.database.data.BookGroup
import io.github.alessandrojean.toshokan.database.data.Person
import io.github.alessandrojean.toshokan.database.data.Publisher
import io.github.alessandrojean.toshokan.database.data.Store
import io.github.alessandrojean.toshokan.util.extension.toLocalCalendar

sealed class SearchFilters {
  data class Complete(
    val query: String = "",
    val isFuture: Boolean? = false,
    val favoritesOnly: Boolean = false,
    val collections: List<String> = emptyList(),
    val groups: List<BookGroup> = emptyList(),
    val publishers: List<Publisher> = emptyList(),
    val contributors: List<Person> = emptyList(),
    val stores: List<Store> = emptyList(),
    val boughtAt: DateRange? = null,
    val readAt: DateRange? = null,
  ) : SearchFilters()

  data class Incomplete(
    val query: String = "",
    val isFuture: Boolean? = false,
    val favoritesOnly: Boolean = false,
    val collections: List<String> = emptyList(),
    val groups: List<Long> = emptyList(),
    val publishers: List<Long> = emptyList(),
    val contributors: List<Long> = emptyList(),
    val stores: List<Long> = emptyList(),
    val boughtAt: DateRange? = null,
    val readAt: DateRange? = null,
  ) : SearchFilters()
}

data class DateRange(
  val start: Long,
  val end: Long
) {

  fun toSelection(): Pair<Long, Long> = Pair(start, end)

  companion object {
    fun fromSelection(selection: Pair<Long, Long>) = DateRange(
      start = selection.first.toLocalCalendar()!!.timeInMillis,
      end = selection.second.toLocalCalendar()!!.timeInMillis
    )
  }

}