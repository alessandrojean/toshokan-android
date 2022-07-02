package io.github.alessandrojean.toshokan.repository

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import io.github.alessandrojean.toshokan.database.ToshokanDatabase
import io.github.alessandrojean.toshokan.database.data.BookGroup
import io.github.alessandrojean.toshokan.domain.RankingItem
import io.github.alessandrojean.toshokan.util.extension.currentTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupsRepository @Inject constructor(
  private val database: ToshokanDatabase
) {

  val groups = database.groupQueries.selectAll().asFlow().mapToList().flowOn(Dispatchers.IO)

  val groupsSorted = database.groupQueries.selectSorted().asFlow().mapToList().flowOn(Dispatchers.IO)

  fun subscribeGroupsNotEmpty(): Flow<List<BookGroup>> {
    return database.groupQueries.selectNotEmpty().asFlow().mapToList().flowOn(Dispatchers.IO)
  }

  private fun nextSortValue(): Int {
    return database.groupQueries.nextSortValue().executeAsOne().expr?.toInt() ?: 0
  }

  fun findAll(): List<BookGroup> {
    return database.groupQueries.selectAll().executeAsList()
  }

  fun findById(id: Long): BookGroup? {
    return database.groupQueries.findById(id).executeAsOneOrNull()
  }

  fun findByIds(ids: List<Long>): List<BookGroup> {
    return database.groupQueries.findByIds(ids).executeAsList()
  }

  fun subscribeToRanking(limit: Long = 20): Flow<List<RankingItem>> {
    return database.groupQueries
      .groupRanking(
        limit = limit,
        mapper = { groupId, groupName, count ->
          RankingItem(itemId = groupId, title = groupName, count = count)
        }
      )
      .asFlow()
      .mapToList()
      .flowOn(Dispatchers.IO)
  }

  suspend fun insert(name: String): Long? = withContext(Dispatchers.IO) {
    val now = currentTime

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
      updated_at = currentTime
    )
  }

  suspend fun updateSort(id: Long, sort: Int) = withContext(Dispatchers.IO) {
    database.groupQueries.updateSort(
      id = id,
      sort = sort,
      updated_at = currentTime
    )
  }

  suspend fun bulkDelete(ids: List<Long>) = withContext(Dispatchers.IO) {
    database.groupQueries.deleteBulk(ids)
  }

}