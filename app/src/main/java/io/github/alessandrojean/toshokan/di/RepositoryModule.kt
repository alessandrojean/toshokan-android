package io.github.alessandrojean.toshokan.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.alessandrojean.toshokan.service.lookup.LookupRepository
import io.github.alessandrojean.toshokan.service.lookup.LookupRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

  @Singleton
  @Provides
  fun provideLookupRepository(): LookupRepository {
    return LookupRepositoryImpl()
  }

}