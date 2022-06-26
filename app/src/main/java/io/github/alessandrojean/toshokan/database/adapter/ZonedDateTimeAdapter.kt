package io.github.alessandrojean.toshokan.database.adapter

import com.squareup.sqldelight.ColumnAdapter
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

class ZonedDateTimeAdapter : ColumnAdapter<ZonedDateTime, Long> {

  override fun decode(databaseValue: Long): ZonedDateTime {
    return ZonedDateTime.ofInstant(Instant.ofEpochMilli(databaseValue), ZoneOffset.UTC)
  }

  override fun encode(value: ZonedDateTime): Long {
    return value.toInstant().toEpochMilli()
  }

}