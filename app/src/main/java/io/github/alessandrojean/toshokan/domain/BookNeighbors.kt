package io.github.alessandrojean.toshokan.domain

import io.github.alessandrojean.toshokan.database.data.Book

data class BookNeighbors(
  val first: Book? = null,
  val previous: Book? = null,
  val current: Book? = null,
  val next: Book? = null,
  val last: Book? = null,
  val count: Int = 0
)