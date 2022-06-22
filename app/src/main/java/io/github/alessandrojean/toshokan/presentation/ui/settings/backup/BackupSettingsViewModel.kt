package io.github.alessandrojean.toshokan.presentation.ui.settings.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.alessandrojean.toshokan.data.backup.BackupRestoreService
import io.github.alessandrojean.toshokan.data.backup.SheetBackupRestorer
import io.github.alessandrojean.toshokan.data.preference.PreferencesManager
import kotlinx.coroutines.launch
import logcat.LogPriority
import logcat.logcat
import javax.inject.Inject

@HiltViewModel
class BackupSettingsViewModel @Inject constructor(
  @ApplicationContext private val context: Context
) : ViewModel() {

  fun restoreFromSheet(uri: Uri) {
    try {
      BackupRestoreService.start(context, uri)
    } catch (e: Exception) {
      logcat(LogPriority.ERROR) { e.stackTraceToString() }
    }
  }

}