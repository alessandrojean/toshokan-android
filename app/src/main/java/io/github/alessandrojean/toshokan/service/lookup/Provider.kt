package io.github.alessandrojean.toshokan.service.lookup

import androidx.annotation.StringRes
import io.github.alessandrojean.toshokan.R

enum class Provider(@StringRes val title: Int, val url: String) {
  CBL(R.string.cbl, "https://www.cblservicos.org.br"),
  GOOGLE_BOOKS(R.string.google_books, "https://books.google.com"),
  OPEN_LIBRARY(R.string.open_library, "https://openlibrary.org"),
  MERCADO_EDITORIAL(R.string.mercado_editorial, "https://mercadoeditorial.org"),
  SKOOB(R.string.skoob, "https://skoob.com.br")
}
