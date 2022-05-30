package io.github.alessandrojean.toshokan.service.cover.wordpress

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WordPressItem(
  @SerialName("_embedded") val embedded: WordPressEmbed? = null
)

@Serializable
data class WordPressEmbed(
  @SerialName("wp:featuredmedia") val featuredMedia: List<WordPressFeaturedMedia>? = null
)

@Serializable
data class WordPressFeaturedMedia(
  @SerialName("source_url") val sourceUrl: String? = null
)
