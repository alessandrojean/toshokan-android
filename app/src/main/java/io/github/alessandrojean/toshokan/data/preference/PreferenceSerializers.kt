package io.github.alessandrojean.toshokan.data.preference

import android.icu.util.Currency
import com.fredporciuncula.flow.preferences.Serializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

class JsonSerializer<T : Any>(
  private val json: Json,
  private val jsonSerializer: KSerializer<T>
) : Serializer<T> {

  override fun deserialize(serialized: String): T {
    return json.decodeFromString(jsonSerializer, serialized)
  }

  override fun serialize(value: T): String {
    return json.encodeToString(jsonSerializer, value)
  }

}

class CurrencySerializer : Serializer<Currency> {

  override fun deserialize(serialized: String): Currency = Currency.getInstance(serialized)

  override fun serialize(value: Currency): String = value.currencyCode

}