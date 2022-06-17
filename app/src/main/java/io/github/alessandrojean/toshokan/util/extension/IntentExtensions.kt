package io.github.alessandrojean.toshokan.util.extension

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import io.github.alessandrojean.toshokan.R

fun Uri.toShareIntent(context: Context, type: String = "image/*", message: String? = null): Intent {
  val uri = this

  val shareIntent = Intent(Intent.ACTION_SEND).apply {
    message?.let { putExtra(Intent.EXTRA_TEXT, message) }
    putExtra(Intent.EXTRA_STREAM, uri)
    clipData = ClipData.newRawUri(message, uri)
    this.type = type
    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
  }

  return Intent.createChooser(shareIntent, context.getString(R.string.action_share)).apply {
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
  }
}