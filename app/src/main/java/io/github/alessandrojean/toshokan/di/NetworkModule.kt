package io.github.alessandrojean.toshokan.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.alessandrojean.toshokan.data.preference.PreferencesManager
import io.ktor.client.HttpClient
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import logcat.LogPriority
import logcat.logcat
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

  @Singleton
  @Provides
  fun provideJson(): Json {
    return Json {
      prettyPrint = true
      ignoreUnknownKeys = true
      isLenient = true
    }
  }

  @Singleton
  @Provides
  fun provideHttpClient(
    preferencesManager: PreferencesManager,
    json: Json
  ): HttpClient {
    return HttpClient {
      expectSuccess = true

      install(HttpCache)

      if (preferencesManager.verboseLogging().get()) {
        install(Logging) {
          logger = object : Logger {
            override fun log(message: String) {
              logcat("HttpClient", LogPriority.INFO) { message }
            }
          }
          level = LogLevel.HEADERS
        }
      }

      install(ContentNegotiation) {
        json(json)
      }
    }
  }

  @Singleton
  @Provides
  fun provideOkHttpClient(
    @ApplicationContext context: Context,
    preferencesManager: PreferencesManager
  ): OkHttpClient {
    val builder =  OkHttpClient.Builder()
      .connectTimeout(30, TimeUnit.SECONDS)
      .readTimeout(30, TimeUnit.SECONDS)
      .callTimeout(2, TimeUnit.MINUTES)
      .cache(Cache(File(context.cacheDir, "network_cache"), 5L * 1024 * 1024 /* 5 MiB */))

    if (preferencesManager.verboseLogging().get()) {
      val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.HEADERS
      }
      builder.addInterceptor(httpLoggingInterceptor)
    }

    return builder.build()
  }

}
