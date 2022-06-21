package io.github.alessandrojean.toshokan.presentation.ui.settings.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.data.preference.PreferencesManager
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibrarySettingsViewModel @Inject constructor(
  preferencesManager: PreferencesManager
) : ViewModel() {

  val showBookNavigation = preferencesManager.showBookNavigation()

  fun onShowBookNavigationChanged(newValue: Boolean) = viewModelScope.launch {
    showBookNavigation.setAndCommit(newValue)
  }

}