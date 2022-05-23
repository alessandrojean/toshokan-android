package io.github.alessandrojean.toshokan.repository

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import io.github.alessandrojean.toshokan.database.ToshokanDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

class PeopleRepository @Inject constructor(
  private val database: ToshokanDatabase
) {

  val people = database.personQueries.selectAll().asFlow().mapToList()

  suspend fun insert(
    name: String,
    description: String,
    country: String,
    website: String,
    instagramProfile: String,
    twitterProfile: String
  ) = withContext(Dispatchers.IO) {
    val now = Date().time

    database.personQueries.insert(
      id = null,
      name = name,
      description = description,
      country = country,
      website = website,
      instagram_profile = instagramProfile,
      twitter_profile = twitterProfile,
      created_at = now,
      updated_at = now
    )
  }

  suspend fun update(
    id: Long,
    name: String,
    description: String,
    country: String,
    website: String,
    instagramProfile: String,
    twitterProfile: String
  ) = withContext(Dispatchers.IO) {
    database.personQueries.update(
      id = id,
      name = name,
      description = description,
      country = country,
      website = website,
      instagram_profile = instagramProfile,
      twitter_profile = twitterProfile,
      updated_at = Date().time
    )
  }

  suspend fun bulkDelete(ids: List<Long>) = withContext(Dispatchers.IO) {
    database.personQueries.deleteBulk(ids)
  }

}