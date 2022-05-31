package io.github.alessandrojean.toshokan.data.preference

import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
  private val flowPrefs: FlowSharedPreferences
) {

  fun isbnLookupSearchHistory() = flowPrefs.getStringSet(
    PreferenceKeys.isbnLookupSearchHistory,
    emptySet()
  )

}