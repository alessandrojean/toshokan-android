package io.github.alessandrojean.toshokan.repository

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import io.github.alessandrojean.toshokan.database.ToshokanDatabase
import io.github.alessandrojean.toshokan.database.data.Tag
import io.github.alessandrojean.toshokan.domain.RankingItem
import io.github.alessandrojean.toshokan.util.extension.currentTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagsRepository @Inject constructor(
  private val database: ToshokanDatabase
) {

  fun subscribeToTags(): Flow<List<Tag>> {
    return database.tagQueries.selectAll().asFlow().mapToList().flowOn(Dispatchers.IO)
  }

  fun subscribeToTagsNotEmpty(): Flow<List<Tag>> {
    return database.tagQueries.selectNotEmpty().asFlow().mapToList().flowOn(Dispatchers.IO)
  }

  fun findById(id: Long): Tag? {
    return database.tagQueries.findById(id).executeAsOneOrNull()
  }

  fun findByIds(ids: List<Long>): List<Tag> {
    return database.tagQueries.findByIds(ids).executeAsList()
  }

  fun findAll(): List<Tag> {
    return database.tagQueries.selectAll().executeAsList()
  }

  fun subscribeToRanking(limit: Long = 20): Flow<List<RankingItem>> {
    return database.tagQueries
      .tagRanking(
        limit = limit,
        mapper = { tagId, tagName, count ->
          RankingItem(itemId = tagId, title = tagName, count = count)
        }
      )
      .asFlow()
      .mapToList()
      .flowOn(Dispatchers.IO)
  }

  suspend fun insert(name: String, isNsfw: Boolean = false): Long? = withContext(Dispatchers.IO) {
    val now = currentTime

    database.tagQueries.insert(
      id = null,
      name = name,
      is_nsfw = isNsfw,
      created_at = now,
      updated_at = now
    )

    database.tagQueries.lastInsertedId().executeAsOne().max
  }

  suspend fun update(id: Long, name: String, isNsfw: Boolean = false) = withContext(Dispatchers.IO) {
    database.tagQueries.update(
      id = id,
      name = name,
      is_nsfw = isNsfw,
      updated_at = currentTime
    )
  }

  suspend fun bulkDelete(ids: List<Long>) = withContext(Dispatchers.IO) {
    database.tagQueries.deleteBulk(ids)
  }

}