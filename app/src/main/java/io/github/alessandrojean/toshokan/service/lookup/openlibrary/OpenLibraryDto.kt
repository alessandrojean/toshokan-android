package io.github.alessandrojean.toshokan.service.lookup.openlibrary

import kotlinx.serialization.Serializable

typealias OpenLibraryResult = Map<String, OpenLibraryBook>

@Serializable
data class OpenLibraryBook(
  val authors: List<OpenLibraryAuthor>? = emptyList(),
  val cover: OpenLibraryCover? = null,
  val identifiers: Map<String, List<String>> = emptyMap(),
  val publishers: List<OpenLibraryPublisher> = emptyList(),
  val title: String,
  val url: String
)

@Serializable
data class OpenLibraryAuthor(val name: String)

@Serializable
data class OpenLibraryPublisher(val name: String)

@Serializable
data class OpenLibraryCover(
  val large: String? = ""
)
