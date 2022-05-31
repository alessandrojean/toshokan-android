package io.github.alessandrojean.toshokan.domain

import io.github.alessandrojean.toshokan.database.data.Person

data class Contributor(
  val person: Person? = null,
  val personId: Long? = null,
  val personText: String = "",
  val role: CreditRole
)
