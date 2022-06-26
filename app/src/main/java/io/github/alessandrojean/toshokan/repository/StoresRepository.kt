package io.github.alessandrojean.toshokan.repository

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import io.github.alessandrojean.toshokan.database.ToshokanDatabase
import io.github.alessandrojean.toshokan.database.data.Store
import io.github.alessandrojean.toshokan.util.extension.currentTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoresRepository @Inject constructor(
  private val database: ToshokanDatabase
) {

  val stores = database.storeQueries.selectAll().asFlow().mapToList().flowOn(Dispatchers.IO)

  fun selectAll(): List<Store> {
    return database.storeQueries.selectAll().executeAsList()
  }

  fun findById(storeId: Long): Store? {
    return database.storeQueries.findById(storeId).executeAsOneOrNull()
  }

  fun findByIds(ids: List<Long>): List<Store> {
    return database.storeQueries.findByIds(ids).executeAsList()
  }

  suspend fun insert(
    name: String,
    description: String = "",
    website: String = "",
    instagramProfile: String = "",
    twitterProfile: String = ""
  ): Long? = withContext(Dispatchers.IO) {
    val now = currentTime

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
      updated_at = currentTime
    )
  }

  suspend fun bulkDelete(ids: List<Long>) = withContext(Dispatchers.IO) {
    database.storeQueries.deleteBulk(ids)
  }

}