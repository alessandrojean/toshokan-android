package io.github.alessandrojean.toshokan.di

import android.app.Application
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.alessandrojean.toshokan.database.ToshokanDatabase
import io.github.alessandrojean.toshokan.data.adapter.BookCreditRoleAdapter
import io.github.alessandrojean.toshokan.data.adapter.CurrencyAdapter
import io.github.alessandrojean.toshokan.database.data.Book
import io.github.alessandrojean.toshokan.database.data.BookCredit
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
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
    return ToshokanDatabase(
      driver = sqlDriver,
      BookAdapter = Book.Adapter(
        paid_price_currencyAdapter = CurrencyAdapter(),
        label_price_currencyAdapter = CurrencyAdapter(),
      ),
      BookCreditAdapter = BookCredit.Adapter(
        roleAdapter = BookCreditRoleAdapter()
      )
    )
  }

}