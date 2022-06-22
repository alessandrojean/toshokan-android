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

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_HIGH
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_LOW
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.util.extension.buildNotificationChannel
import io.github.alessandrojean.toshokan.util.extension.buildNotificationChannelGroup

object Notifications {

  /**
   * Notification channel and ids used by the backup and restore system.
   */
  private const val GROUP_BACKUP_RESTORE = "group_backup_restore"
  const val CHANNEL_BACKUP_RESTORE_PROGRESS = "backup_restore_progress_channel"
  const val ID_BACKUP_PROGRESS = -101
  const val ID_RESTORE_PROGRESS = -103
  const val CHANNEL_BACKUP_RESTORE_COMPLETE = "backup_restore_complete_channel"
  const val ID_BACKUP_COMPLETE = -102
  const val ID_RESTORE_COMPLETE = -104

  fun createChannels(context: Context) {
    val notificationService = NotificationManagerCompat.from(context)

    notificationService.createNotificationChannelGroupsCompat(
      listOf(
        buildNotificationChannelGroup(GROUP_BACKUP_RESTORE) {
          setName(context.getString(R.string.label_backup))
        }
      )
    )

    notificationService.createNotificationChannelsCompat(
      listOf(
        buildNotificationChannel(CHANNEL_BACKUP_RESTORE_PROGRESS, IMPORTANCE_LOW) {
          setName(context.getString(R.string.channel_progress))
          setGroup(GROUP_BACKUP_RESTORE)
          setShowBadge(false)
        },
        buildNotificationChannel(CHANNEL_BACKUP_RESTORE_COMPLETE, IMPORTANCE_HIGH) {
          setName(context.getString(R.string.channel_complete))
          setGroup(GROUP_BACKUP_RESTORE)
          setShowBadge(false)
          setSound(null, null)
        },
      )
    )
  }

}