package io.github.alessandrojean.toshokan.presentation.ui.settings.covers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.data.preference.PreferencesManager
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoversSettingsViewModel @Inject constructor(
  preferencesManager: PreferencesManager
) : ViewModel() {

  val disabledCoverProviders = preferencesManager.disabledCoverProviders()

  fun onDisabledCoverProvidersChanged(newValue: Set<String>) = viewModelScope.launch {
    disabledCoverProviders.setAndCommit(newValue)
  }

}