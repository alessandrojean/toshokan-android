package io.github.alessandrojean.toshokan.data.preference

import android.icu.util.Currency
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.github.alessandrojean.toshokan.BuildConfig
import kotlinx.serialization.serializer
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
  private val dataStore: DataStore<Preferences>
) {

  fun isbnLookupSearchHistory() = dataStore.getObject(
    serializer = DataStoreJsonSerializer<List<String>>(serializer()),
    key = PreferenceKeys.isbnLookupSearchHistory,
    defaultValue = emptyList()
  )

  fun showBookNavigation() = dataStore.getBoolean(
    key = PreferenceKeys.showBookNavigation,
    defaultValue = true
  )

  fun disabledLookupProviders() = dataStore.getStringSet(
    key = PreferenceKeys.disabledLookupProviders,
    defaultValue = emptySet()
  )

  fun disabledCoverProviders() = dataStore.getStringSet(
    key = PreferenceKeys.disabledCoverProviders,
    defaultValue = emptySet()
  )

  fun verboseLogging() = dataStore.getBoolean(
    key = PreferenceKeys.verboseLogging,
    defaultValue = BuildConfig.FLAVOR == "dev"
  )

  fun currency() = dataStore.getObject(
    key = PreferenceKeys.currency,
    serializer = object : DataStoreObjectSerializer<Currency> {
      override fun serialize(value: Currency): String = value.currencyCode
      override fun deserialize(encoded: String): Currency = Currency.getInstance(encoded)
    },
    defaultValue = Currency.getInstance(Locale.getDefault())
  )

  fun theme() = dataStore.getEnum(
    key = PreferenceKeys.theme,
    defaultValue = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      Theme.FOLLOW_SYSTEM
    } else {
      Theme.LIGHT
    }
  )

}