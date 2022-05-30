package io.github.alessandrojean.toshokan.service.cover.oembed

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OembedInformation(
  @SerialName("thumbnail_url") val thumbnailUrl: String? = null
)
