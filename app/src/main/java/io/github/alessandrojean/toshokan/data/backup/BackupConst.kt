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

import io.github.alessandrojean.toshokan.BuildConfig.APPLICATION_ID as ID

object BackupConst {

  private const val NAME = "BackupRestoreServices"
  const val EXTRA_URI = "$ID.$NAME.EXTRA_URI"

}