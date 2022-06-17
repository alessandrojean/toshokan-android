package io.github.alessandrojean.toshokan.util.extension

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import io.github.alessandrojean.toshokan.BuildConfig
import java.io.File

fun File.getUriCompat(context: Context): Uri {
  return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", this)
}