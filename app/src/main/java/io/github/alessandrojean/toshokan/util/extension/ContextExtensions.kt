package io.github.alessandrojean.toshokan.util.extension

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.PowerManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
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

/**
 * Helper method to create a notification builder.
 *
 * @param channelId the channel id.
 * @param block the function that will execute inside the builder.
 * @return a notification to be displayed or updated.
 */
fun Context.notificationBuilder(
  channelId: String,
  block: (NotificationCompat.Builder.() -> Unit)? = null
): NotificationCompat.Builder {
  // TODO: Set the accent color.
  val builder = NotificationCompat.Builder(this, channelId)

  block?.let {
    builder.block()
  }

  return builder
}

val Context.notificationManager: NotificationManager
  get() = getSystemService()!!

val Context.powerManager: PowerManager
  get() = getSystemService()!!

/**
 * Returns true if the given service class is running.
 */
fun Context.isServiceRunning(serviceClass: Class<*>): Boolean {
  val className = serviceClass.name
  val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
  @Suppress("DEPRECATION")
  return manager.getRunningServices(Integer.MAX_VALUE)
    .any { className == it.service.className }
}


/**
 * Convenience method to acquire a partial wake lock.
 */
@SuppressLint("WakelockTimeout")
fun Context.acquireWakeLock(tag: String): PowerManager.WakeLock {
  val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "$tag:WakeLock")
  wakeLock.acquire()
  return wakeLock
}
