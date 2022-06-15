package io.github.alessandrojean.toshokan.presentation.ui.settings.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.data.preference.PreferencesManager
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchSettingsViewModel @Inject constructor(
  preferencesManager: PreferencesManager
) : ViewModel() {

  val enabledLookupProviders = preferencesManager.enabledLookupProviders()

  fun onEnabledLookupProvidersChanged(newValue: Set<String>) = viewModelScope.launch {
    enabledLookupProviders.setAndCommit(newValue)
  }

}