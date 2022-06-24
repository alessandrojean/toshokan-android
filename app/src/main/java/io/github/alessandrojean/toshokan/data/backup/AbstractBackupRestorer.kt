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
import android.net.Uri
import androidx.annotation.StringRes
import kotlinx.coroutines.Job

abstract class AbstractBackupRestorer(
  protected val context: Context,
  protected val notifier: BackupNotifier
) {

  var job: Job? = null

  protected var restoreAmount = 0
  protected var restoreProgress = 0

  @Throws(SheetRestoreException::class)
  abstract suspend fun performRestore(uri: Uri): Boolean

  suspend fun restoreBackup(uri: Uri): Boolean {
    val startTime = System.currentTimeMillis()
    restoreProgress = 0

    if (!performRestore(uri)) {
      return false
    }

    val endTime = System.currentTimeMillis()
    val time = endTime - startTime

    notifier.showRestoreComplete(time)
    return true
  }

  internal fun showRestoreProgress(progress: Int, amount: Int, title: String) {
    notifier.showRestoreProgress(title, progress, amount)
  }

  class SheetRestoreException(message: String) : Exception(message)

}