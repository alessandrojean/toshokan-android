package io.github.alessandrojean.toshokan.util.extension

import android.content.Context
import io.github.alessandrojean.toshokan.R
import java.text.DateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Convert UTC time millisecond to Calendar instance in local time zone
 *
 * @return local Calendar instance at supplied UTC time. Null if time is 0.
 */
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
  format: Int = DateFormat.SHORT,
  timeZone: TimeZone = TimeZone.getTimeZone("UTC")
): String {
  if (this == 0L) {
    return ""
  }

  val dateFormat = DateFormat.getDateInstance(format, locale).apply {
    this.timeZone = timeZone
  }
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

val SHEET_TIME_ZONE: ZoneId = ZoneId.of("America/Sao_Paulo")

fun String.toSheetDate(): Long? {
  return runCatching { LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE) }
    .getOrNull()
    ?.atTime(0, 0)
    ?.atZone(SHEET_TIME_ZONE)
    ?.withZoneSameInstant(ZoneOffset.UTC)
    ?.toInstant()
    ?.toEpochMilli()
}

fun Long.toUtcEpochMilli(): Long =
  LocalDateTime
    .ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
    .atZone(ZoneId.systemDefault())
    .withZoneSameInstant(ZoneOffset.UTC)
    .toInstant()
    .toEpochMilli()

fun Long.toLocalEpochMilli(): Long =
  LocalDateTime
    .ofInstant(Instant.ofEpochMilli(this), ZoneOffset.UTC)
    .atZone(ZoneOffset.UTC)
    .withZoneSameInstant(ZoneId.systemDefault())
    .toInstant()
    .toEpochMilli()

val currentTime: Long
  get () = LocalDateTime.now()
    .atZone(ZoneId.systemDefault())
    .withZoneSameInstant(ZoneOffset.UTC)
    .toInstant()
    .toEpochMilli()

val firstDayOfCurrentMonth: Long
  get () = LocalDate.now()
    .with(TemporalAdjusters.firstDayOfMonth())
    .atTime(0, 0)
    .atZone(ZoneId.systemDefault())
    .withZoneSameInstant(ZoneOffset.UTC)
    .toInstant()
    .toEpochMilli()

val lastDayOfCurrentMonth: Long
  get () = LocalDate.now()
    .with(TemporalAdjusters.lastDayOfMonth())
    .atTime(0, 0)
    .atZone(ZoneId.systemDefault())
    .withZoneSameInstant(ZoneOffset.UTC)
    .toInstant()
    .toEpochMilli()

/**
 * Check if both dates are in the same month and year.
 * The dates must be in the UTC timezone.
 */
fun isStartAndEndOfSameMonth(start: Long, end: Long, zoneId: ZoneId = ZoneOffset.UTC): Boolean {
  val startDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(start), zoneId)
  val endDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(end), zoneId)

  if (!isSameMonth(startDate, endDate)) {
    return false
  }

  val monthStart = startDate.with(TemporalAdjusters.firstDayOfMonth())
  val monthEnd = endDate.with(TemporalAdjusters.lastDayOfMonth())

  return startDate.equals(monthStart) && endDate.equals(monthEnd)
}

/**
 * Check if both dates are in the same month and year.
 * The dates must be in the UTC timezone.
 */
fun isSameMonth(start: LocalDateTime, end: LocalDateTime): Boolean {
  return YearMonth.from(start).equals(YearMonth.from(end))
}

/**
 * Check if the date is in the current month.
 */
fun isInCurrentMonth(date: Long, zoneId: ZoneId = ZoneOffset.UTC): Boolean {
  val localDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(date), zoneId)

  return YearMonth.now(ZoneOffset.UTC).equals(YearMonth.from(localDate))
}

/**
 * Get a localized month-year string.
 */
fun Long.toLocalizedMonthYear(
  context: Context,
  zoneId: ZoneId = ZoneOffset.UTC,
  locale: Locale = Locale.getDefault()
): String {
  val pattern = context.getString(R.string.period_full_month_pattern)
  val dateTimeFormatter = DateTimeFormatter.ofPattern(pattern, locale)

  val localDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), zoneId)
  val yearMonth = YearMonth.from(localDate)

  return yearMonth.format(dateTimeFormatter).replaceFirstChar { it.uppercase(locale) }
}