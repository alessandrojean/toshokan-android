package io.github.alessandrojean.toshokan

import android.app.Application
import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.util.DebugLogger
import dagger.hilt.android.HiltAndroidApp
import io.github.alessandrojean.toshokan.data.cache.CoverCache
import io.github.alessandrojean.toshokan.data.coil.BookCoverFetcher
import io.github.alessandrojean.toshokan.data.coil.BookCoverKeyer
import io.github.alessandrojean.toshokan.data.notification.Notifications
import logcat.AndroidLogcatLogger
import logcat.LogPriority
import logcat.LogPriority.VERBOSE
import logcat.logcat
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class ToshokanApp : Application(), ImageLoaderFactory {

  @Inject lateinit var okHttpClient: OkHttpClient
  @Inject lateinit var coverCache: CoverCache

  override fun onCreate() {
    super.onCreate()

    // Log all priorities in debug builds, no-op in release builds.
    AndroidLogcatLogger.installOnDebuggableApp(this, minPriority = VERBOSE)

    setupNotificationChannels()
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

  protected open fun setupNotificationChannels() {
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
