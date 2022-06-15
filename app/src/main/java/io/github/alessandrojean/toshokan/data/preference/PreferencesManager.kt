package io.github.alessandrojean.toshokan.data.preference

import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import io.github.alessandrojean.toshokan.service.lookup.Provider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
  private val flowPrefs: FlowSharedPreferences
) {

  fun isbnLookupSearchHistory() = flowPrefs.getStringSet(
    PreferenceKeys.isbnLookupSearchHistory,
    defaultValue = emptySet()
  )

  fun showBookNavigation() = flowPrefs.getBoolean(
    PreferenceKeys.showBookNavigation,
    defaultValue = true
  )

  fun enabledLookupProviders() = flowPrefs.getStringSet(
    PreferenceKeys.enabledLookupProviders,
    defaultValue = Provider.values().map(Provider::name).toSet()
  )

}