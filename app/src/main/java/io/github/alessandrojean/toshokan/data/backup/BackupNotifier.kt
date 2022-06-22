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

import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.data.notification.NotificationReceiver
import io.github.alessandrojean.toshokan.data.notification.Notifications
import io.github.alessandrojean.toshokan.util.extension.notificationBuilder
import io.github.alessandrojean.toshokan.util.extension.notificationManager
import java.util.concurrent.TimeUnit

class BackupNotifier constructor(private val context: Context) {

  private val progressNotificationBuilder =
    context.notificationBuilder(Notifications.CHANNEL_BACKUP_RESTORE_PROGRESS) {
      setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
      setSmallIcon(R.drawable.ic_settings_backup_restore_24dp)
      setAutoCancel(false)
      setOngoing(true)
      setOnlyAlertOnce(true)
    }

  private val completeNotificationBuilder =
    context.notificationBuilder(Notifications.CHANNEL_BACKUP_RESTORE_COMPLETE) {
      setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
      setSmallIcon(R.drawable.ic_settings_backup_restore_24dp)
      setAutoCancel(false)
    }

  fun showRestoreProgress(content: String = "", progress: Int = 0, maxAmount: Int = 100): NotificationCompat.Builder {
    val builder = with(progressNotificationBuilder) {
      setContentTitle(context.getString(R.string.restoring_backup))
      setContentText(content)
      setProgress(maxAmount, progress, false)
      setOnlyAlertOnce(true)

      // Clear old actions if they exist
      clearActions()

      addAction(
        R.drawable.ic_close_24dp,
        context.getString(R.string.action_cancel),
        NotificationReceiver.cancelRestorePendingBroadcast(context, Notifications.ID_RESTORE_PROGRESS)
      )
    }

    builder.show(Notifications.ID_RESTORE_PROGRESS)

    return builder
  }

  fun showRestoreError(error: String?) {
    context.notificationManager.cancel(Notifications.ID_RESTORE_PROGRESS)

    with(completeNotificationBuilder) {
      setContentTitle(context.getString(R.string.error_during_backup_restore))
      setContentText(error)

      show(Notifications.ID_RESTORE_COMPLETE)
    }
  }

  fun showRestoreComplete(time: Long) {
    context.notificationManager.cancel(Notifications.ID_RESTORE_PROGRESS)

    val timeString = context.getString(
      R.string.restore_duration,
      TimeUnit.MILLISECONDS.toMinutes(time),
      TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(
        TimeUnit.MILLISECONDS.toMinutes(time)
      )
    )

    with(completeNotificationBuilder) {
      setContentTitle(context.getString(R.string.restore_completed))
      setContentText(context.getString(R.string.restore_completed_message, timeString))

      clearActions()

      show(Notifications.ID_RESTORE_COMPLETE)
    }
  }

  private fun NotificationCompat.Builder.show(id: Int) {
    context.notificationManager.notify(id, build())
  }

}