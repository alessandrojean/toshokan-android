package io.github.alessandrojean.toshokan.data.notification

/**
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

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import io.github.alessandrojean.toshokan.data.backup.BackupRestoreService
import io.github.alessandrojean.toshokan.util.extension.notificationManager
import io.github.alessandrojean.toshokan.BuildConfig.APPLICATION_ID as ID

class NotificationReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    when (intent.action) {
      ACTION_CANCEL_RESTORE -> cancelRestore(context, intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1))
    }
  }

  /**
   * Dismiss the notification
   *
   * @param notificationId the id of the notification
   */
  private fun dismissNotification(context: Context, notificationId: Int) {
    context.notificationManager.cancel(notificationId)
  }

  /**
   * Method called when user wants to stop a backup restore job.
   *
   * @param context context of application
   * @param notificationId id of notification
   */
  private fun cancelRestore(context: Context, notificationId: Int) {
    BackupRestoreService.stop(context)
    ContextCompat.getMainExecutor(context).execute { dismissNotification(context, notificationId) }
  }

  companion object {
    private const val NAME = "NotificationReceiver"

    private const val ACTION_CANCEL_RESTORE = "$ID.$NAME.CANCEL_RESTORE"

    private const val EXTRA_NOTIFICATION_ID = "$ID.$NAME.NOTIFICATION_ID"

    /**
     * Returns [PendingIntent] that cancels a backup restore job.
     *
     * @param context context of application
     * @param notificationId id of notification
     * @return [PendingIntent]
     */
    internal fun cancelRestorePendingBroadcast(context: Context, notificationId: Int): PendingIntent {
      val intent = Intent(context, NotificationReceiver::class.java).apply {
        action = ACTION_CANCEL_RESTORE
        putExtra(EXTRA_NOTIFICATION_ID, notificationId)
      }

      return PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
    }
  }

}