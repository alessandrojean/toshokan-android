package io.github.alessandrojean.toshokan.util.extension

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import io.github.alessandrojean.toshokan.R

fun Uri.toShareImageIntent(
  context: Context,
  mimeType: String = "image/*",
  message: String? = null
): Intent {
  val uri = this

  val shareIntent = Intent(Intent.ACTION_SEND).apply {
    type = mimeType
    message?.let { putExtra(Intent.EXTRA_TEXT, message) }
    putExtra(Intent.EXTRA_STREAM, uri)
    clipData = ClipData.newRawUri(message.orEmpty(), uri)
    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
  }

  return Intent.createChooser(shareIntent, context.getString(R.string.action_share)).apply {
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
  }
}

fun Uri.toShareIntent(context: Context, subject: String? = null): Intent {
  val link = toString()

  val shareIntent = Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
    putExtra(Intent.EXTRA_TEXT, link)
  }

  return Intent.createChooser(shareIntent, context.getString(R.string.action_share)).apply {
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
  }
}