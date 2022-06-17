package io.github.alessandrojean.toshokan.util.extension

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.annotation.StringRes
import io.github.alessandrojean.toshokan.R
import java.io.File

/**
 * The common cached images directory.
 */
val Context.cacheImagesDir: File
  get() = File(cacheDir, "shared_images")

/**
 * The app name.
 */
val Context.appName: String
  get() = getString(R.string.app_name)

/**
 * Check if the device has a camera.
 */
val Context.deviceHasCamera: Boolean
  get() = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

/**
 * Display a toast in this context.
 *
 * Code from Tachiyomi 0.x.
 *
 * @param resource the text resource
 * @param duration the duration of the toast. Defaults to short.
 */
fun Context.toast(
  @StringRes resource: Int,
  duration: Int = Toast.LENGTH_SHORT,
  block: (Toast) -> Unit = {}
): Toast {
  return toast(getString(resource), duration, block)
}

/**
 * Display a toast in this context.
 *
 * @param text the text to display
 * @param duration the duration of the toast. Defaults to short.
 */
fun Context.toast(
  text: String?,
  duration: Int = Toast.LENGTH_SHORT,
  block: (Toast) -> Unit = {}
): Toast {
  return Toast.makeText(this, text.orEmpty(), duration).also {
    block.invoke(it)
    it.show()
  }
}