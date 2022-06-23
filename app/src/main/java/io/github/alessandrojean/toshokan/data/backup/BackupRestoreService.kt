package io.github.alessandrojean.toshokan.data.backup

/**
 * Protobuf backup restorer.
 *
 * Based on Tachiyomi's implementation.
 * https://github.com/tachiyomiorg/tachiyomi
 *
 * Copyright 2015 Javier Tomás
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.os.PowerManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.data.notification.Notifications
import io.github.alessandrojean.toshokan.util.extension.acquireWakeLock
import io.github.alessandrojean.toshokan.util.extension.isServiceRunning
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import logcat.LogPriority
import logcat.logcat
import javax.inject.Inject

@AndroidEntryPoint
class BackupRestoreService : Service() {

  @Inject lateinit var sheetBackupRestorerFactory: SheetBackupRestorer.Factory

  companion object {
    /**
     * Returns the status of the service.
     *
     * @param context the application context.
     * @return true if the service is running, false otherwise.
     */
    fun isRunning(context: Context): Boolean =
      context.isServiceRunning(BackupRestoreService::class.java)

    /**
     * Starts a service to restore a backup from Json
     *
     * @param context context of application
     * @param uri path of Uri
     */
    fun start(context: Context, uri: Uri) {
      if (!isRunning(context)) {
        val intent = Intent(context, BackupRestoreService::class.java).apply {
          putExtra(BackupConst.EXTRA_URI, uri)
        }
        ContextCompat.startForegroundService(context, intent)
      }
    }

    /**
     * Stops the service.
     *
     * @param context the application context.
     */
    fun stop(context: Context) {
      context.stopService(Intent(context, BackupRestoreService::class.java))

      BackupNotifier(context).showRestoreError(context.getString(R.string.restoring_backup_canceled))
    }
  }

  /**
   * Wake lock that will be held until the service is destroyed.
   */
  private lateinit var wakeLock: PowerManager.WakeLock

  private lateinit var ioScope: CoroutineScope
  private var backupRestore: AbstractBackupRestorer? = null
  private lateinit var notifier: BackupNotifier

  override fun onCreate() {
    super.onCreate()

    ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    notifier = BackupNotifier(this)
    wakeLock = acquireWakeLock(javaClass.name)

    startForeground(Notifications.ID_RESTORE_PROGRESS, notifier.showRestoreProgress().build())
  }

  override fun stopService(name: Intent?): Boolean {
    destroyJob()
    return super.stopService(name)
  }

  override fun onDestroy() {
    destroyJob()
    super.onDestroy()
  }

  private fun destroyJob() {
    backupRestore?.job?.cancel()
    ioScope.cancel()
    if (wakeLock.isHeld) {
      wakeLock.release()
    }
  }

  /**
   * This method needs to be implemented, but it's not used/needed.
   */
  override fun onBind(p0: Intent?): IBinder? = null

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val uri = intent?.getParcelableExtra<Uri>(BackupConst.EXTRA_URI) ?: return START_NOT_STICKY

    backupRestore?.job?.cancel()

    backupRestore = sheetBackupRestorerFactory.create(this, notifier)

    val handler = CoroutineExceptionHandler { _, exception ->
      logcat(LogPriority.ERROR) { exception.stackTraceToString() }
      notifier.showRestoreError(exception.message)
      stopSelf(startId)
    }

    val job = ioScope.launch(handler) {
      if (backupRestore?.restoreBackup(uri) == false) {
        notifier.showRestoreError(getString(R.string.restoring_backup_canceled))
      }
    }

    job.invokeOnCompletion {
      stopSelf(startId)
    }

    backupRestore?.job = job

    return START_NOT_STICKY
  }

}