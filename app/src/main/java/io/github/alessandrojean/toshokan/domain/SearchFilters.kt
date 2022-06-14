package io.github.alessandrojean.toshokan.domain

import androidx.core.util.Pair
import io.github.alessandrojean.toshokan.database.data.BookGroup
import io.github.alessandrojean.toshokan.database.data.Person
import io.github.alessandrojean.toshokan.database.data.Publisher
import io.github.alessandrojean.toshokan.database.data.Store
import io.github.alessandrojean.toshokan.util.extension.toLocalCalendar

data class SearchFilters(
  val query: String = "",
  val isFuture: Boolean? = false,
  val favoritesOnly: Boolean = false,
  val groups: List<BookGroup> = emptyList(),
  val publishers: List<Publisher> = emptyList(),
  val contributors: List<Person> = emptyList(),
  val stores: List<Store> = emptyList(),
  val boughtAt: DateRange? = null,
  val readAt: DateRange? = null,
)

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