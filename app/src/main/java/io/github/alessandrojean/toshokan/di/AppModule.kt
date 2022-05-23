package io.github.alessandrojean.toshokan.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.alessandrojean.toshokan.ToshokanApp
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

  @Singleton
  @Provides
  fun provideApplication(@ApplicationContext app: Context): ToshokanApp {
    return app as ToshokanApp
  }
}