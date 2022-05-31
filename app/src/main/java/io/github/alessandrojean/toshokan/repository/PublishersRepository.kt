package io.github.alessandrojean.toshokan.repository

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import io.github.alessandrojean.toshokan.database.ToshokanDatabase
import io.github.alessandrojean.toshokan.database.data.Publisher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PublishersRepository @Inject constructor(
  private val database: ToshokanDatabase
) {

  val publishers = database.publisherQueries.selectAll().asFlow().mapToList()

  fun selectAll(): List<Publisher> {
    return database.publisherQueries.selectAll().executeAsList()
  }

  fun findByName(name: String): Publisher? {
    return database.publisherQueries.findByName(name).executeAsOneOrNull()
  }

  suspend fun insert(
    name: String,
    description: String = "",
    website: String = "",
    instagramProfile: String = "",
    twitterProfile: String = ""
  ): Long? = withContext(Dispatchers.IO) {
    val now = Date().time

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
      updated_at = Date().time
    )
  }

  suspend fun bulkDelete(ids: List<Long>) = withContext(Dispatchers.IO) {
    database.publisherQueries.deleteBulk(ids)
  }

}