package io.github.alessandrojean.toshokan.presentation.ui.settings.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.ui.settings.components.SettingsListScaffold
import io.github.alessandrojean.toshokan.presentation.ui.settings.components.SwitchPreference
import io.github.alessandrojean.toshokan.util.extension.collectAsStateWithLifecycle

class LibrarySettingsScreen : AndroidScreen() {

  @Composable
  override fun Content() {
    val viewModel = getViewModel<LibrarySettingsViewModel>()
    val navigator = LocalNavigator.currentOrThrow

    val showBookNavigation by viewModel.showBookNavigationFlow
      .collectAsStateWithLifecycle(initialValue = true)

    SettingsListScaffold(
      title = stringResource(R.string.settings_library),
      onNavigationClick = { navigator.pop() }
    ) {
      item("show_book_navigation") {
        SwitchPreference(
          title = stringResource(R.string.pref_show_book_navigation),
          checked = showBookNavigation,
          onCheckedChange = { viewModel.onShowBookNavigationChanged(it) }
        )
      }
    }
  }

}