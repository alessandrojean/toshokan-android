package io.github.alessandrojean.toshokan.service.lookup.openlibrary

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

typealias OpenLibraryResult = Map<String, OpenLibraryBook>

@Serializable
data class OpenLibraryBook(
  val authors: List<OpenLibraryContributor>? = emptyList(),
  val cover: OpenLibraryCover? = null,
  val identifiers: Map<String, List<String>> = emptyMap(),
  @SerialName("number_of_pages") val pageCount: Int? = 0,
  val publishers: List<OpenLibraryPublisher> = emptyList(),
  val title: String,
  val url: String
)

@Serializable
data class OpenLibraryBookDetails(
  @SerialName("physical_dimensions") val physicalDimensions: String? = "",
  val contributors: List<OpenLibraryContributor>? = emptyList(),
  val description: JsonElement? = null
)

@Serializable
data class OpenLibraryContributor(
  val name: String,
  val role: String? = null
)

@Serializable
data class OpenLibraryPublisher(val name: String)

@Serializable
data class OpenLibraryCover(
  val large: String? = ""
)
