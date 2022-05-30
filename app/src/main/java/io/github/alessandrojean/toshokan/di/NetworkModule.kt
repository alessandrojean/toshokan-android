package io.github.alessandrojean.toshokan.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

  @Singleton
  @Provides
  fun provideHttpClient(): HttpClient {
    return HttpClient {
      expectSuccess = true

      install(HttpCache)

      install(Logging) {
        logger = object : Logger {
          override fun log(message: String) {
            logcat("HttpClient", LogPriority.INFO) { message }
          }
        }
        level = LogLevel.HEADERS
      }

      install(ContentNegotiation) {
        json(
          Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            isLenient = true
          }
        )
      }
    }
  }

}
