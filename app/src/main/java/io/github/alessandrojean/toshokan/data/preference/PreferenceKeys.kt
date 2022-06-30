package io.github.alessandrojean.toshokan.data.preference

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

object PreferenceKeys {

  val isbnLookupSearchHistory = stringPreferencesKey("isbn_lookup_search_history_v2")

  val theme = stringPreferencesKey("theme")

  val currency = stringPreferencesKey("currency")

  val showBookNavigation = booleanPreferencesKey("show_book_navigation")

  val disabledLookupProviders = stringSetPreferencesKey("disabled_lookup_providers")

  val disabledCoverProviders = stringSetPreferencesKey("disabled_cover_providers")

  val verboseLogging = booleanPreferencesKey("verbose_logging")

}