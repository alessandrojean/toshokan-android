package io.github.alessandrojean.toshokan.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.alessandrojean.toshokan.network.HttpClient
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

  @Singleton
  @Provides
  fun provideHttpClient(): HttpClient {
    return HttpClient
  }

  @Singleton
  @Provides
  fun provideDefaultOkHttpClient(): OkHttpClient {
    return HttpClient.default
  }

}
