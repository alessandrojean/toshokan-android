package io.github.alessandrojean.toshokan.database.adapter

import com.squareup.sqldelight.ColumnAdapter
import io.github.alessandrojean.toshokan.domain.CreditRole

class BookCreditRoleAdapter : ColumnAdapter<CreditRole, Long> {

  override fun decode(databaseValue: Long): CreditRole {
    return CreditRole.values().firstOrNull { it.code == databaseValue } ?: CreditRole.UNKNOWN
  }

  override fun encode(value: CreditRole): Long = value.code

}