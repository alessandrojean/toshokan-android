package io.github.alessandrojean.toshokan.domain

import io.github.alessandrojean.toshokan.database.data.Book

data class Library(
  val groups: Map<LibraryGroup, List<Book>> = emptyMap()
)

data class LibraryGroup(
  val id: Long,
  val name: String,
  val sort: Int
)
