package io.github.alessandrojean.toshokan.repository

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import io.github.alessandrojean.toshokan.database.ToshokanDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupsRepository @Inject constructor(
  private val database: ToshokanDatabase
) {

  val groups = database.groupQueries.selectAll().asFlow().mapToList()

  val groupsSorted = database.groupQueries.selectSorted().asFlow().mapToList()

  private fun nextSortValue(): Long {
    return database.groupQueries.nextSortValue().executeAsOne().expr ?: 0L
  }

  suspend fun insert(name: String) = withContext(Dispatchers.IO) {
    val now = Date().time

    database.groupQueries.insert(
      id = null,
      name = name,
      sort = nextSortValue(),
      created_at = now,
      updated_at = now
    )
  }

  suspend fun update(id: Long, name: String) = withContext(Dispatchers.IO) {
    database.groupQueries.updateName(
      id = id,
      name = name,
      updated_at = Date().time
    )
  }

  suspend fun updateSort(id: Long, sort: Long) = withContext(Dispatchers.IO) {
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