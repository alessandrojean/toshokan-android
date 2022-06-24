package io.github.alessandrojean.toshokan.util.extension

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

fun Long.toLocalCalendar(): Calendar? {
  if (this == 0L) {
    return null
  }

  val rawCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
    timeInMillis = this@toLocalCalendar
  }

  return Calendar.getInstance().apply {
    clear()
    set(
      rawCalendar.get(Calendar.YEAR),
      rawCalendar.get(Calendar.MONTH),
      rawCalendar.get(Calendar.DAY_OF_MONTH),
      rawCalendar.get(Calendar.HOUR_OF_DAY),
      rawCalendar.get(Calendar.MINUTE),
      rawCalendar.get(Calendar.SECOND)
    )
  }
}

fun Long.formatToLocaleDate(
  locale: Locale = Locale.getDefault(),
  format: Int = DateFormat.SHORT
): String {
  if (this == 0L) {
    return ""
  }

  val dateFormat = DateFormat.getDateInstance(format, locale)
  return runCatching { dateFormat.format(this) }
    .getOrNull() ?: ""
}

fun Long.formatToLocaleDateTime(
  locale: Locale = Locale.getDefault(),
  dateStyle: Int = DateFormat.MEDIUM,
  timeStyle: Int = DateFormat.SHORT,
  timeZone: TimeZone = TimeZone.getDefault()
): String? {
  if (this == 0L) {
    return null
  }

  val dateTimeFormat = DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale).apply {
    this.timeZone = timeZone
  }

  return runCatching { dateTimeFormat.format(this) }.getOrNull()
}

private val SHEET_DATE_PARSER by lazy {
  SimpleDateFormat("yyyy-MM-dd", Locale.US)
}

fun String.toSheetDate(): Long? {
  return runCatching { SHEET_DATE_PARSER.parse(this)?.time }.getOrNull()
}