package io.github.alessandrojean.toshokan.di

import android.app.Application
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.alessandrojean.toshokan.database.ToshokanDatabase
import io.github.alessandrojean.toshokan.repository.PeopleRepository
import io.github.alessandrojean.toshokan.repository.PublishersRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

  @Singleton
  @Provides
  fun provideDriver(app: Application): SqlDriver {
    return AndroidSqliteDriver(
      schema = ToshokanDatabase.Schema,
      context = app,
      name = "toshokan.db"
    )
  }

  @Singleton
  @Provides
  fun provideDatabase(sqlDriver: SqlDriver): ToshokanDatabase {
    return ToshokanDatabase(sqlDriver)
  }

}