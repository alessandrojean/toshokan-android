package io.github.alessandrojean.toshokan

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.util.DebugLogger
import dagger.hilt.android.HiltAndroidApp
import io.github.alessandrojean.toshokan.data.cache.CoverCache
import io.github.alessandrojean.toshokan.data.coil.BookCoverFetcher
import io.github.alessandrojean.toshokan.data.coil.BookCoverKeyer
import io.github.alessandrojean.toshokan.data.notification.Notifications
import io.github.alessandrojean.toshokan.data.preference.PreferencesManager
import io.github.alessandrojean.toshokan.data.preference.Theme
import kotlinx.coroutines.flow.launchIn
import logcat.AndroidLogcatLogger
import logcat.LogPriority
import logcat.LogPriority.VERBOSE
import logcat.LogcatLogger
import logcat.logcat
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class ToshokanApp : Application(), ImageLoaderFactory {

  @Inject lateinit var okHttpClient: OkHttpClient
  @Inject lateinit var coverCache: CoverCache
  @Inject lateinit var preferences: PreferencesManager

  override fun onCreate() {
    super.onCreate()

    // Log all priorities in debug builds, no-op in release builds.
    if (!LogcatLogger.isInstalled && preferences.verboseLogging().get()) {
      LogcatLogger.install(AndroidLogcatLogger(VERBOSE))
    }

    setupNotificationChannels()

    preferences.theme()
      .asImmediateObjectFlow {
        AppCompatDelegate.setDefaultNightMode(
          when (it) {
            Theme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            Theme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            Theme.FOLLOW_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
          }
        )
      }
      .launchIn(ProcessLifecycleOwner.get().lifecycleScope)
  }

  override fun newImageLoader(): ImageLoader {
    val builder = ImageLoader.Builder(this).apply {
      val callFactoryInit = { okHttpClient }
      val diskCacheInit = { CoilDiskCache.get(this@ToshokanApp) }

      callFactory(callFactoryInit)
      diskCache(diskCacheInit)

      components {
        add(BookCoverFetcher.Factory(coverCache, lazy(callFactoryInit), lazy(diskCacheInit)))
        add(BookCoverKeyer())
      }

      if (BuildConfig.DEBUG) {
        logger(DebugLogger())
      }
    }

    return builder.build()
  }

  private fun setupNotificationChannels() {
    try {
      Notifications.createChannels(this)
    } catch (e: Exception) {
      logcat(LogPriority.ERROR) { "Failed to modify notification channels" }
    }
  }

}

/**
 * Direct copy of Coil's internal SingletonDiskCache so that [BookCoverFetcher] can access it.
 */
internal object CoilDiskCache {

  private const val FOLDER_NAME = "image_cache"
  private var instance: DiskCache? = null

  @Synchronized
  fun get(context: Context): DiskCache {
    return instance ?: run {
      val safeCacheDir = context.cacheDir.apply { mkdirs() }
      // Create the singleton disk cache instance.
      DiskCache.Builder()
        .directory(safeCacheDir.resolve(FOLDER_NAME))
        .build()
        .also { instance = it }
    }
  }
}
