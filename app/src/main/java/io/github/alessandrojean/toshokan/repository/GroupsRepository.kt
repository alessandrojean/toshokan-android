package io.github.alessandrojean.toshokan.repository

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import io.github.alessandrojean.toshokan.database.ToshokanDatabase
import io.github.alessandrojean.toshokan.database.data.BookGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupsRepository @Inject constructor(
  private val database: ToshokanDatabase
) {

  val groups = database.groupQueries.selectAll().asFlow().mapToList()

  val groupsSorted = database.groupQueries.selectSorted().asFlow().mapToList()

  private fun nextSortValue(): Int {
    return database.groupQueries.nextSortValue().executeAsOne().expr?.toInt() ?: 0
  }

  fun findById(id: Long): BookGroup? {
    return database.groupQueries.findById(id).executeAsOneOrNull()
  }

  fun findByIds(ids: List<Long>): List<BookGroup> {
    return database.groupQueries.findByIds(ids).executeAsList()
  }

  suspend fun insert(name: String): Long? = withContext(Dispatchers.IO) {
    val now = Date().time

    database.groupQueries.insert(
      id = null,
      name = name,
      sort = nextSortValue(),
      created_at = now,
      updated_at = now
    )

    database.groupQueries.lastInsertedId().executeAsOne().max
  }

  suspend fun update(id: Long, name: String) = withContext(Dispatchers.IO) {
    database.groupQueries.updateName(
      id = id,
      name = name,
      updated_at = Date().time
    )
  }

  suspend fun updateSort(id: Long, sort: Int) = withContext(Dispatchers.IO) {
    database.groupQueries.updateSort(
      id = id,
      sort = sort,
      updated_at = Date().time
    )
  }

  suspend fun bulkDelete(ids: List<Long>) = withContext(Dispatchers.IO) {
    database.groupQueries.deleteBulk(ids)
  }

}