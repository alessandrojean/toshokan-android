package io.github.alessandrojean.toshokan.util.extension

import android.icu.text.MessageFormat
import java.text.NumberFormat
import java.util.Locale

fun String.parseLocaleValueOrNull(locale: Locale = Locale.getDefault()): Float? {
  val numberFormat = NumberFormat.getNumberInstance(locale)

  return runCatching { numberFormat.parse(this)?.toFloat()  }.getOrNull()
}

fun Int.toLocaleString(
  locale: Locale = Locale.getDefault(),
  options: NumberFormat.() -> Unit = {}
): String {
  return this.toFloat().toLocaleString(locale) {
    maximumFractionDigits = 0
    options()
  }
}

fun Long.toLocaleString(
  locale: Locale = Locale.getDefault(),
  options: NumberFormat.() -> Unit = {}
): String {
  return this.toFloat().toLocaleString(locale) {
    maximumFractionDigits = 0
    options()
  }
}

fun Float.toLocaleString(
  locale: Locale = Locale.getDefault(),
  options: NumberFormat.() -> Unit = {}
): String {
  val numberFormat = NumberFormat.getNumberInstance(locale)
  options.invoke(numberFormat)

  return numberFormat.format(this.toDouble())
}

fun Float.toLocaleCurrencyString(
  currency: String,
  locale: Locale = Locale.getDefault(),
  options: android.icu.text.NumberFormat.() -> Unit = {}
): String {
  return toLocaleCurrencyString(
    currency = android.icu.util.Currency.getInstance(currency),
    locale = locale,
    options = options
  )
}

fun Float.toLocaleCurrencyString(
  currency: android.icu.util.Currency,
  locale: Locale = Locale.getDefault(),
  options: android.icu.text.NumberFormat.() -> Unit = {}
): String {
  val numberFormat = android.icu.text.NumberFormat.getCurrencyInstance(locale).apply {
    minimumFractionDigits = 2
    maximumFractionDigits = 2
    this.currency = currency
  }

  options.invoke(numberFormat)

  return numberFormat.format(this.toDouble())
}

fun Int.toOrdinal(locale: Locale = Locale.getDefault()): String {
  return MessageFormat("{0,ordinal}", locale).format(arrayOf(this))
}