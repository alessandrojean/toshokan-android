package io.github.alessandrojean.toshokan.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

  @Singleton
  @Provides
  fun provideHttpClient(): OkHttpClient {
    val logging = HttpLoggingInterceptor()
      .apply { level = HttpLoggingInterceptor.Level.BODY }

    return OkHttpClient.Builder()
      .addInterceptor(logging)
      .build()
  }

}
