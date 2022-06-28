package io.github.alessandrojean.toshokan.service.lookup.googlebooks

import kotlinx.serialization.Serializable

@Serializable
data class GoogleBooksResult(
  val items: List<GoogleBooksVolume>? = emptyList()
)

@Serializable
data class GoogleBooksVolume(
  val id: String,
  val volumeInfo: GoogleBooksVolumeInfo
)

@Serializable
data class GoogleBooksVolumeInfo(
  val authors: List<String>? = emptyList(),
  val description: String? = "",
  val dimensions: GoogleBooksDimensions? = null,
  val industryIdentifiers: List<GoogleBooksIndustryIdentifier> = emptyList(),
  val pageCount: Int? = 0,
  val publisher: String? = "",
  val title: String
)

@Serializable
data class GoogleBooksIndustryIdentifier(
  val type: String,
  val identifier: String
)

@Serializable
data class GoogleBooksDimensions(
  val height: String,
  val width: String
)