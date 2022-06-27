package io.github.alessandrojean.toshokan.presentation.ui.settings.advanced

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alessandrojean.toshokan.BuildConfig
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.data.preference.PreferenceKeys
import io.github.alessandrojean.toshokan.presentation.ui.settings.components.SettingsListScaffold
import io.github.alessandrojean.toshokan.presentation.ui.settings.components.SwitchPreference
import io.github.alessandrojean.toshokan.util.extension.collectAsStateWithLifecycle
import io.github.alessandrojean.toshokan.util.extension.toast

class AdvancedSettingsScreen : AndroidScreen() {

  @Composable
  override fun Content() {
    val viewModel = getViewModel<AdvancedSettingsViewModel>()
    val navigator = LocalNavigator.currentOrThrow
    val activity = LocalContext.current as? AppCompatActivity

    val verboseLogging by viewModel.verboseLogging.asFlow()
      .collectAsStateWithLifecycle(initialValue = BuildConfig.DEBUG)

    SettingsListScaffold(
      title = stringResource(R.string.settings_advanced),
      onNavigationClick = { navigator.pop() }
    ) {
      item(PreferenceKeys.showBookNavigation) {
        SwitchPreference(
          title = stringResource(R.string.pref_verbose_logging),
          summary = stringResource(R.string.pref_verbose_logging_summary),
          checked = verboseLogging,
          onCheckedChange = {
            viewModel.onVerboseLoggingChanged(it)
            activity?.toast(R.string.requires_app_restart)
          }
        )
      }
    }
  }

}