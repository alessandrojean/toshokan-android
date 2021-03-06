package io.github.alessandrojean.toshokan.presentation.ui.settings.advanced

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.data.preference.PreferencesManager
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdvancedSettingsViewModel @Inject constructor(
  preferencesManager: PreferencesManager
) : ViewModel() {

  private val verboseLogging = preferencesManager.verboseLogging()
  val verboseLoggingFlow = verboseLogging.asFlow()

  fun onVerboseLoggingChanged(newValue: Boolean) = viewModelScope.launch {
    verboseLogging.edit(newValue)
  }

}