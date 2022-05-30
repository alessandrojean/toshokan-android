package io.github.alessandrojean.toshokan.service.lookup

import androidx.annotation.StringRes
import io.github.alessandrojean.toshokan.R

enum class Provider(@StringRes val title: Int) {
  CBL(R.string.cbl),
  GOOGLE_BOOKS(R.string.google_books),
  OPEN_LIBRARY(R.string.open_library),
  MERCADO_EDITORIAL(R.string.mercado_editorial),
  SKOOB(R.string.skoob)
}
