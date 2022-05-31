package io.github.alessandrojean.toshokan.repository

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import io.github.alessandrojean.toshokan.database.ToshokanDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoresRepository @Inject constructor(
  private val database: ToshokanDatabase
) {

  val stores = database.storeQueries.selectAll().asFlow().mapToList()

  suspend fun insert(
    name: String,
    description: String = "",
    website: String = "",
    instagramProfile: String = "",
    twitterProfile: String = ""
  ): Long? = withContext(Dispatchers.IO) {
    val now = Date().time

    database.storeQueries.insert(
      id = null,
      name = name,
      description = description,
      website = website,
      instagram_profile = instagramProfile,
      twitter_profile = twitterProfile,
      created_at = now,
      updated_at = now
    )

    database.storeQueries.lastInsertedId().executeAsOne().max
  }

  suspend fun update(
    id: Long,
    name: String,
    description: String,
    website: String,
    instagramProfile: String,
    twitterProfile: String
  ) = withContext(Dispatchers.IO) {
    database.storeQueries.update(
      id = id,
      name = name,
      description = description,
      website = website,
      instagram_profile = instagramProfile,
      twitter_profile = twitterProfile,
      updated_at = Date().time
    )
  }

  suspend fun bulkDelete(ids: List<Long>) = withContext(Dispatchers.IO) {
    database.storeQueries.deleteBulk(ids)
  }

}