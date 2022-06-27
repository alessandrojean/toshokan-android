package io.github.alessandrojean.toshokan.presentation.ui.settings.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.data.backup.SheetBackupRestorer
import io.github.alessandrojean.toshokan.presentation.ui.settings.components.GenericPreference
import io.github.alessandrojean.toshokan.presentation.ui.settings.components.SettingsListScaffold
import io.github.alessandrojean.toshokan.util.extension.toast

class BackupSettingsScreen : AndroidScreen() {

  @Composable
  override fun Content() {
    val viewModel = getViewModel<BackupSettingsViewModel>()
    val navigator = LocalNavigator.currentOrThrow
    val context = LocalContext.current

    val sheetBackupPickerLauncher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.GetContent(),
      onResult = { uri ->
        uri?.let {
          viewModel.restoreFromSheet(it)
        }
      }
    )

    SettingsListScaffold(
      title = stringResource(R.string.settings_backup),
      onNavigationClick = { navigator.pop() }
    ) {
      item("restore_from_sheet") {
        GenericPreference(
          title = stringResource(R.string.pref_restore_from_sheet),
          summary = stringResource(R.string.pref_restore_from_sheet_summary),
          onClick = {
            if (!viewModel.restoreRunning) {
              sheetBackupPickerLauncher.launch(SheetBackupRestorer.BACKUP_MIME)
            } else {
              context.toast(R.string.restore_in_progress)
            }
          }
        )
      }
    }
  }

}