package io.github.alessandrojean.toshokan.service.link

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class BookLink(
  @StringRes val name: Int,
  @DrawableRes val icon: Int? = null,
  val category: LinkCategory,
  val url: String
)
