package io.github.alessandrojean.toshokan.domain

import io.github.alessandrojean.toshokan.database.data.Tag

data class RawTag(
  val tag: Tag? = null,
  val tagId: Long? = null,
  val tagText: String = "",
)
