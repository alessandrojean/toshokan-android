package io.github.alessandrojean.toshokan.presentation.ui.settings.general

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.data.backup.BackupRestoreService
import io.github.alessandrojean.toshokan.data.preference.PreferenceKeys
import io.github.alessandrojean.toshokan.presentation.ui.settings.components.GenericPreference
import io.github.alessandrojean.toshokan.presentation.ui.settings.components.SettingsScaffold
import io.github.alessandrojean.toshokan.presentation.ui.settings.components.SwitchPreference
import io.github.alessandrojean.toshokan.service.cover.BookCover
import io.github.alessandrojean.toshokan.util.extension.collectAsStateWithLifecycle

class GeneralSettingsScreen : AndroidScreen() {

  @Composable
  override fun Content() {
    val navigator = LocalNavigator.currentOrThrow
    val context = LocalContext.current

    SettingsScaffold(
      title = stringResource(R.string.settings_general),
      onNavigationClick = { navigator.pop() }
    ) {
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