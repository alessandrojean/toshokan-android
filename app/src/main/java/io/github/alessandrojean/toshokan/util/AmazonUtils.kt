package io.github.alessandrojean.toshokan.util

import io.github.alessandrojean.toshokan.service.cover.amazon.AmazonCoverProvider.Companion.AMAZON_IMAGES_URL
import io.github.alessandrojean.toshokan.service.cover.amazon.AmazonCoverProvider.Companion.AMAZON_IMAGE_SIZE

fun String.toAmazonCoverUrl(): String? {
  if (!isValidIsbn()) {
    return null
  }

  return "$AMAZON_IMAGES_URL/${toIsbn10()}.01._SCRM_${AMAZON_IMAGE_SIZE}_.jpg"
}
