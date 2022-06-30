package io.github.alessandrojean.toshokan.repository

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import io.github.alessandrojean.toshokan.database.ToshokanDatabase
import io.github.alessandrojean.toshokan.database.data.Publisher
import io.github.alessandrojean.toshokan.util.extension.currentTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PublishersRepository @Inject constructor(
  private val database: ToshokanDatabase
) {

  val publishers = database.publisherQueries.selectAll().asFlow().mapToList().flowOn(Dispatchers.IO)

  fun selectAll(): List<Publisher> {
    return database.publisherQueries.selectAll().executeAsList()
  }

  fun findByName(name: String): Publisher? {
    return database.publisherQueries.findByName(name).executeAsOneOrNull()
  }

  fun findById(id: Long): Publisher? {
    return database.publisherQueries.findById(id).executeAsOneOrNull()
  }

  fun findByIds(ids: List<Long>): List<Publisher> {
    return database.publisherQueries.findByIds(ids).executeAsList()
  }

  suspend fun insert(
    name: String,
    description: String = "",
    website: String = "",
    instagramProfile: String = "",
    twitterProfile: String = ""
  ): Long? = withContext(Dispatchers.IO) {
    val now = currentTime

    database.publisherQueries.insert(
      name = name,
      description = description,
      website = website,
      instagram_user = instagramProfile,
      twitter_user = twitterProfile,
      created_at = now,
      updated_at = now
    )

    database.publisherQueries.lastInsertedId().executeAsOne().max
  }

  suspend fun update(
    id: Long,
    name: String,
    description: String,
    website: String,
    instagramProfile: String,
    twitterProfile: String
  ) = withContext(Dispatchers.IO) {
    database.publisherQueries.update(
      id = id,
      name = name,
      description = description,
      website = website,
      instagram_user = instagramProfile,
      twitter_user = twitterProfile,
      updated_at = currentTime
    )
  }

  suspend fun bulkDelete(ids: List<Long>) = withContext(Dispatchers.IO) {
    database.publisherQueries.deleteBulk(ids)
  }

}