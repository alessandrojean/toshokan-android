package io.github.alessandrojean.toshokan.presentation.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.outlined.ManageSearch
import androidx.compose.material.icons.outlined.SettingsBackupRestore
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.ui.settings.components.SettingsCategory
import io.github.alessandrojean.toshokan.presentation.ui.settings.components.SettingsScaffold
import io.github.alessandrojean.toshokan.presentation.ui.settings.covers.CoversSettingsScreen
import io.github.alessandrojean.toshokan.presentation.ui.settings.library.LibrarySettingsScreen
import io.github.alessandrojean.toshokan.presentation.ui.settings.search.SearchSettingsScreen

class SettingsScreen : AndroidScreen() {

  @Composable
  override fun Content() {
    val navigator = LocalNavigator.currentOrThrow

    SettingsScaffold(
      title = stringResource(R.string.settings),
      onNavigationClick = { navigator.pop() }
    ) {
      item {
        SettingsCategory(
          title = stringResource(R.string.settings_general),
          icon = Icons.Outlined.Tune,
          onClick = {}
        )
      }
      item {
        SettingsCategory(
          title = stringResource(R.string.settings_library),
          icon = Icons.Outlined.CollectionsBookmark,
          onClick = { navigator.push(LibrarySettingsScreen()) }
        )
      }
      item {
        SettingsCategory(
          title = stringResource(R.string.settings_search),
          icon = Icons.Outlined.ManageSearch,
          onClick = { navigator.push(SearchSettingsScreen()) }
        )
      }
      item {
        SettingsCategory(
          title = stringResource(R.string.settings_covers),
          icon = Icons.Outlined.ImageSearch,
          onClick = { navigator.push(CoversSettingsScreen()) }
        )
      }
      item {
        SettingsCategory(
          title = stringResource(R.string.settings_backup),
          icon = Icons.Outlined.SettingsBackupRestore,
          onClick = {}
        )
      }
      item {
        SettingsCategory(
          title = stringResource(R.string.settings_advanced),
          icon = Icons.Outlined.Code,
          onClick = {}
        )
      }
    }
  }

}