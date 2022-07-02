package io.github.alessandrojean.toshokan.di

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.hilt.ScreenModelFactory
import cafe.adriel.voyager.hilt.ScreenModelFactoryKey
import cafe.adriel.voyager.hilt.ScreenModelKey
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.multibindings.IntoMap
import io.github.alessandrojean.toshokan.presentation.ui.book.BookScreenModel
import io.github.alessandrojean.toshokan.presentation.ui.book.manage.ManageBookScreenModel
import io.github.alessandrojean.toshokan.presentation.ui.book.reading.ReadingScreenModel
import io.github.alessandrojean.toshokan.presentation.ui.library.LibraryScreenModel
import io.github.alessandrojean.toshokan.presentation.ui.search.SearchScreenModel
import io.github.alessandrojean.toshokan.presentation.ui.statistics.StatisticsScreenModel
import io.github.alessandrojean.toshokan.presentation.ui.statistics.ranking.RankingScreenModel

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

  @Binds
  @IntoMap
  @ScreenModelFactoryKey(ManageBookScreenModel.Factory::class)
  abstract fun bindManageBookScreenModelFactory(
    manageBookScreenModelFactory: ManageBookScreenModel.Factory
  ): ScreenModelFactory

  @Binds
  @IntoMap
  @ScreenModelFactoryKey(SearchScreenModel.Factory::class)
  abstract fun bindSearchScreenModelFactory(
    searchScreenModelFactory: SearchScreenModel.Factory
  ): ScreenModelFactory

  @Binds
  @IntoMap
  @ScreenModelFactoryKey(RankingScreenModel.Factory::class)
  abstract fun bindRankingScreenModelFactory(
    rankingScreenModelFactory: RankingScreenModel.Factory
  ): ScreenModelFactory

  @Binds
  @IntoMap
  @ScreenModelKey(LibraryScreenModel::class)
  abstract fun bindLibraryScreenModel(libraryScreenModel: LibraryScreenModel): ScreenModel

  @Binds
  @IntoMap
  @ScreenModelKey(StatisticsScreenModel::class)
  abstract fun bindStatisticsScreenModel(statisticsScreenModel: StatisticsScreenModel): ScreenModel

}