package io.github.alessandrojean.toshokan.presentation.ui.settings.general

import android.icu.util.Currency
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.data.preference.PreferencesManager
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GeneralSettingsViewModel @Inject constructor(
  private val preferencesManager: PreferencesManager
) : ViewModel() {

  val currency = preferencesManager.currency().asFlow()

  fun onCurrencyChanged(newValue: Currency) = viewModelScope.launch {
    preferencesManager.currency().setAndCommit(newValue)
  }

}