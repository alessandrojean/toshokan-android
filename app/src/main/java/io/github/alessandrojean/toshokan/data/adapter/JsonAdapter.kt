package io.github.alessandrojean.toshokan.data.adapter

import com.squareup.sqldelight.ColumnAdapter
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

class JsonAdapter<T : Any>(
  private val json: Json,
  private val serializer: KSerializer<T>
) : ColumnAdapter<T, String> {

  override fun decode(databaseValue: String): T {
    return json.decodeFromString(serializer, databaseValue)
  }

  override fun encode(value: T): String {
    return json.encodeToString(serializer, value)
  }

}