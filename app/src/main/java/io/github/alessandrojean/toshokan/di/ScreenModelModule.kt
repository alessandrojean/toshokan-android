package io.github.alessandrojean.toshokan.di

import cafe.adriel.voyager.hilt.ScreenModelFactory
import cafe.adriel.voyager.hilt.ScreenModelFactoryKey
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.multibindings.IntoMap
import io.github.alessandrojean.toshokan.presentation.ui.book.BookScreenModel
import io.github.alessandrojean.toshokan.presentation.ui.book.reading.ReadingScreenModel

@Module
@InstallIn(ActivityComponent::class)
abstract class ScreenModelModule {

  @Binds
  @IntoMap
  @ScreenModelFactoryKey(ReadingScreenModel.Factory::class)
  abstract fun bindReadingScreenModelFactory(
    readingScreenModelFactory: ReadingScreenModel.Factory
  ): ScreenModelFactory

  @Binds
  @IntoMap
  @ScreenModelFactoryKey(BookScreenModel.Factory::class)
  abstract fun bindBookScreenModelFactory(
    bookScreenModelFactory: BookScreenModel.Factory
  ): ScreenModelFactory

}