package io.github.alessandrojean.toshokan.repository

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import io.github.alessandrojean.toshokan.database.ToshokanDatabase
import io.github.alessandrojean.toshokan.database.data.Person
import io.github.alessandrojean.toshokan.util.extension.currentTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PeopleRepository @Inject constructor(
  private val database: ToshokanDatabase
) {

  val people = database.personQueries.selectAll().asFlow().mapToList().flowOn(Dispatchers.IO)

  fun selectAll(): List<Person> {
    return database.personQueries.selectAll().executeAsList()
  }

  fun findByName(name: String): Person? {
    return database.personQueries.findByName(name).executeAsOneOrNull()
  }

  fun findByIds(ids: List<Long>): List<Person> {
    return database.personQueries.findByIds(ids).executeAsList()
  }

  suspend fun insert(
    name: String,
    description: String = "",
    country: String = "",
    website: String = "",
    instagramProfile: String = "",
    twitterProfile: String = ""
  ): Long? = withContext(Dispatchers.IO) {
    val now = currentTime

    database.personQueries.insert(
      name = name,
      description = description,
      country = country,
      website = website,
      instagram_profile = instagramProfile,
      twitter_profile = twitterProfile,
      created_at = now,
      updated_at = now
    )

    database.personQueries.lastInsertedId().executeAsOne().max
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
      updated_at = currentTime
    )
  }

  suspend fun bulkDelete(ids: List<Long>) = withContext(Dispatchers.IO) {
    database.personQueries.deleteBulk(ids)
  }

}