package io.github.alessandrojean.toshokan.presentation.ui.settings.general

import android.content.Intent
import android.icu.util.Currency
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.data.preference.Theme
import io.github.alessandrojean.toshokan.presentation.ui.core.dialog.FullScreenItemPickerDialog
import io.github.alessandrojean.toshokan.presentation.ui.settings.components.GenericPreference
import io.github.alessandrojean.toshokan.presentation.ui.settings.components.ListPreference
import io.github.alessandrojean.toshokan.presentation.ui.settings.components.SettingsListScaffold
import io.github.alessandrojean.toshokan.util.extension.collectAsStateWithLifecycle
import java.text.Collator
import java.util.Locale

class GeneralSettingsScreen : AndroidScreen() {

  @Composable
  override fun Content() {
    val viewModel = getViewModel<GeneralSettingsViewModel>()
    val navigator = LocalNavigator.currentOrThrow
    val context = LocalContext.current
    val currentLocale = Locale.getDefault()

    val collator = Collator.getInstance(currentLocale)

    val currencies = remember {
      Currency
        .getAvailableCurrencies()
        .sortedWith(compareBy(collator) { it.getDisplayName(currentLocale) })
    }

    var showCurrencyPicker by remember { mutableStateOf(false) }
    val currency by viewModel.currencyFlow.collectAsStateWithLifecycle(
      initialValue = Currency.getInstance(currentLocale)
    )
    val theme by viewModel.themeFlow.collectAsStateWithLifecycle(Theme.FOLLOW_SYSTEM)

    FullScreenItemPickerDialog(
      visible = showCurrencyPicker,
      title = stringResource(R.string.currency),
      selected = currency,
      items = currencies,
      itemKey = { it.currencyCode },
      itemText = { it.getDisplayName(currentLocale) },
      onChoose = { viewModel.onCurrencyChanged(it) },
      onDismiss = { showCurrencyPicker = false },
      initialSearch = currency.currencyCode,
      search = { query, fullList ->
        fullList.filter {
          it.currencyCode.startsWith(query, ignoreCase = true) ||
            it.getDisplayName(currentLocale).contains(query, ignoreCase = true)
        }
      },
      searchPlaceholder = stringResource(R.string.currency_search_tip)
    )

    SettingsListScaffold(
      title = stringResource(R.string.settings_general),
      onNavigationClick = { navigator.pop() }
    ) {
      item("theme") {
        ListPreference(
          title = stringResource(R.string.pref_theme),
          summary = stringResource(theme.title),
          selected = theme,
          options = remember { Theme.themes },
          optionKey = { it.name },
          optionText = { context.getString(it.title) },
          onSelectedChange = {
            viewModel.onThemeChanged(it)
          }
        )
      }

      item("currency") {
        GenericPreference(
          title = stringResource(R.string.pref_currency),
          summary = stringResource(R.string.pref_currency_summary),
          onClick = { showCurrencyPicker = true }
        )
      }

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        item("manage_notifications") {
          GenericPreference(
            title = stringResource(R.string.pref_manage_notifications),
            onClick = {
              val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
              }

              context.startActivity(intent)
            }
          )
        }
      }
    }
  }

}