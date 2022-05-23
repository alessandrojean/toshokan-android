package io.github.alessandrojean.toshokan.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.alessandrojean.toshokan.service.lookup.LookupRepository
import io.github.alessandrojean.toshokan.service.lookup.LookupRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

  @Binds
  @Singleton
  abstract fun bindLookupRepository(
    lookupRepositoryImpl: LookupRepositoryImpl
  ): LookupRepository

}