package io.github.alessandrojean.toshokan.data.preference

import android.content.SharedPreferences
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import io.github.alessandrojean.toshokan.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
  private val prefs: SharedPreferences,
  private val flowPrefs: FlowSharedPreferences
) {

  fun isbnLookupSearchHistory() = flowPrefs.getString(
    PreferenceKeys.isbnLookupSearchHistory,
    defaultValue = "[]"
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