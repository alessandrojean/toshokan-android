package io.github.alessandrojean.toshokan

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import logcat.AndroidLogcatLogger
import logcat.LogPriority.VERBOSE

@HiltAndroidApp
class ToshokanApp : Application(), ImageLoaderFactory {

  override fun onCreate() {
    super.onCreate()

    // Log all priorities in debug builds, no-op in release builds.
    AndroidLogcatLogger.installOnDebuggableApp(this, minPriority = VERBOSE)
  }

  override fun newImageLoader(): ImageLoader {
    val builder = ImageLoader.Builder(this).apply {
      diskCache()
    }

    return builder.build()
  }


}
