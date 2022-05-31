package io.github.alessandrojean.toshokan.util.extension

import java.text.NumberFormat
import java.util.Locale

fun String.parseLocaleValueOrNull(locale: Locale = Locale.getDefault()): Float? {
  val numberFormat = NumberFormat.getNumberInstance(locale)

  return runCatching { numberFormat.parse(this)?.toFloat()  }.getOrNull()
}

fun Float.toLocaleString(locale: Locale = Locale.getDefault()): String {
  val numberFormat = NumberFormat.getNumberInstance(locale)

  return numberFormat.format(this.toDouble())
}