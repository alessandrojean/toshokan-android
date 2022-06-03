package io.github.alessandrojean.toshokan.domain

data class Library(
  val groups: Map<LibraryGroup, List<LibraryBook>> = emptyMap()
)

data class LibraryGroup(
  val id: Long,
  val name: String,
  val sort: Int
)

data class LibraryBook(
  val id: Long,
  val title: String,
  val volume: String?,
  val coverUrl: String?,
  val isFuture: Boolean
)
