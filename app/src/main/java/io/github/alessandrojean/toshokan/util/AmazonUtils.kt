package io.github.alessandrojean.toshokan.util

private const val AMAZON_IMAGES_URL = "https://images-na.ssl-images-amazon.com/images/P"
private const val AMAZON_IMAGE_SIZE = "SL700"

fun String.toAmazonCoverUrl(): String? {
  if (!isValidIsbn()) {
    return null
  }

  return "$AMAZON_IMAGES_URL/${toIsbn10()}.01._SCRM_${AMAZON_IMAGE_SIZE}_.jpg"
}
