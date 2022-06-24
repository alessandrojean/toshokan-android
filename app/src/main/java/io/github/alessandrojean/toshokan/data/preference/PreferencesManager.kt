package io.github.alessandrojean.toshokan.data.preference

import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import com.fredporciuncula.flow.preferences.Serializer
import io.github.alessandrojean.toshokan.BuildConfig
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
  private val flowPrefs: FlowSharedPreferences,
  private val json: Json
) {

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

  fun isbnLookupSearchHistory() = flowPrefs.getObject(
    PreferenceKeys.isbnLookupSearchHistory,
    serializer = JsonSerializer<List<String>>(json, serializer()),
    defaultValue = emptyList()
  )

  fun showBookNavigation() = flowPrefs.getBoolean(
    PreferenceKeys.showBookNavigation,
    defaultValue = true
  )

  fun disabledLookupProviders() = flowPrefs.getStringSet(
    PreferenceKeys.disabledLookupProviders,
    defaultValue = emptySet()
  )

  fun disabledCoverProviders() = flowPrefs.getStringSet(
    PreferenceKeys.disabledCoverProviders,
    defaultValue = emptySet()
  )

  fun verboseLogging() = flowPrefs.getBoolean(
    PreferenceKeys.verboseLogging,
    defaultValue = BuildConfig.FLAVOR == "dev"
  )

}