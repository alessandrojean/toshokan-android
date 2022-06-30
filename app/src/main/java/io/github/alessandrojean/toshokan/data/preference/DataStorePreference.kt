package io.github.alessandrojean.toshokan.data.preference

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

open class DataStorePreference<T: Any>(
  private val dataStore: DataStore<Preferences>,
  private val key: Preferences.Key<T>,
  private val defaultValue: T
) {

  open fun asFlow(): Flow<T> = dataStore.getAsFlow(key, defaultValue)

  open fun asImmediateFlow(block: (T) -> Unit): Flow<T> {
    block(get())
    return asFlow().onEach { block(it) }
  }

  open fun get(): T = runBlocking { dataStore.data.first()[key] ?: defaultValue }

  open suspend fun edit(newValue: T) = dataStore.putValue(key, newValue)

}

class DataStoreObjectPreference<T : Any>(
  dataStore: DataStore<Preferences>,
  key: Preferences.Key<String>,
  defaultValue: T,
  private val serializer : DataStoreObjectSerializer<T>
) : DataStorePreference<String>(dataStore, key, serializer.serialize(defaultValue)) {

  fun asObjectFlow(): Flow<T> {
    return asFlow().map { serializer.deserialize(it) }
  }

  fun asImmediateObjectFlow(block: (T) -> Unit): Flow<T> {
    block(getObject())
    return asObjectFlow().onEach { block(it) }
  }

  fun getObject(): T = serializer.deserialize(get())

  suspend fun editObject(newValue: T) {
    edit(serializer.serialize(newValue))
  }

}

interface DataStoreObjectSerializer<T : Any> {
  fun serialize(value: T): String
  fun deserialize(encoded: String): T
}

class DataStoreJsonSerializer<T : Any>(
  private val serializer: KSerializer<T>,
  private val json: Json = Json
) : DataStoreObjectSerializer<T> {
  override fun serialize(value: T): String = json.encodeToString(serializer, value)
  override fun deserialize(encoded: String): T = json.decodeFromString(serializer, encoded)
}

fun <T : Any> DataStore<Preferences>.getAsFlow(
  key: Preferences.Key<T>,
  defaultValue: T
): Flow<T> {
  return data.map { it[key] ?: defaultValue }.flowOn(Dispatchers.IO)
}

suspend fun <T : Any> DataStore<Preferences>.putValue(
  key: Preferences.Key<T>,
  value: T
) {
  edit { settings -> settings[key] = value }
}


private fun <T: Any> DataStore<Preferences>.get(
  key: Preferences.Key<T>,
  defaultValue: T
): DataStorePreference<T> = DataStorePreference(
  dataStore = this,
  key = key,
  defaultValue = defaultValue
)

fun DataStore<Preferences>.getBoolean(
  key: Preferences.Key<Boolean>,
  defaultValue: Boolean
): DataStorePreference<Boolean> = get(key, defaultValue)

fun DataStore<Preferences>.getString(
  key: Preferences.Key<String>,
  defaultValue: String
): DataStorePreference<String> = get(key, defaultValue)

fun DataStore<Preferences>.getStringSet(
  key: Preferences.Key<Set<String>>,
  defaultValue: Set<String>
): DataStorePreference<Set<String>> = get(key, defaultValue)

fun <T: Any> DataStore<Preferences>.getObject(
  key: Preferences.Key<String>,
  serializer: DataStoreObjectSerializer<T>,
  defaultValue: T
): DataStoreObjectPreference<T> = DataStoreObjectPreference(
  dataStore = this,
  key = key,
  defaultValue = defaultValue,
  serializer = serializer
)

inline fun <reified T : Enum<T>> DataStore<Preferences>.getEnum(
  key: Preferences.Key<String>,
  defaultValue: T
): DataStoreObjectPreference<T> = getObject(
  key = key,
  serializer = object : DataStoreObjectSerializer<T> {
    override fun serialize(value: T): String = value.name
    override fun deserialize(encoded: String): T = enumValueOf(encoded)
  },
  defaultValue = defaultValue
)