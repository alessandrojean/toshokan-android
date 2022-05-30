package io.github.alessandrojean.toshokan.service.cover

import androidx.annotation.StringRes

data class SimpleBookInfo(
  val code: String = "",
  val title: String = "",
  val publisher: String = "",
  val initialCovers: List<CoverResult> = emptyList()
)

data class CoverResult(
  @StringRes val source: Int? = null,
  val imageUrl: String
)
