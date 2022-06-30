package io.github.alessandrojean.toshokan.presentation.ui.settings.general

import android.icu.util.Currency
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.data.preference.PreferencesManager
import io.github.alessandrojean.toshokan.data.preference.Theme
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GeneralSettingsViewModel @Inject constructor(
  preferencesManager: PreferencesManager
) : ViewModel() {

  private val theme = preferencesManager.theme()
  val themeFlow = theme.asObjectFlow()

  private val currency = preferencesManager.currency()
  val currencyFlow = currency.asObjectFlow()

  fun onCurrencyChanged(newValue: Currency) = viewModelScope.launch {
    currency.editObject(newValue)
  }

  fun onThemeChanged(newValue: Theme) = viewModelScope.launch {
    theme.editObject(newValue)
  }

}