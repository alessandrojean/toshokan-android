package io.github.alessandrojean.toshokan.database.adapter

import android.icu.util.Currency
import com.squareup.sqldelight.ColumnAdapter

class CurrencyAdapter : ColumnAdapter<Currency, String> {

  override fun decode(databaseValue: String): Currency = Currency.getInstance(databaseValue)

  override fun encode(value: Currency): String = value.currencyCode

}