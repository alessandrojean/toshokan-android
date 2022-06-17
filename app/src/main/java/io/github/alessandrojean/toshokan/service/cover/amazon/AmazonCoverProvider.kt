package io.github.alessandrojean.toshokan.service.cover.amazon

import android.graphics.BitmapFactory
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.service.cover.BookCover
import io.github.alessandrojean.toshokan.service.cover.CoverProvider
import io.github.alessandrojean.toshokan.service.cover.CoverProviderWebsite
import io.github.alessandrojean.toshokan.service.cover.SimpleBookInfo
import io.github.alessandrojean.toshokan.util.isValidIsbn
import io.github.alessandrojean.toshokan.util.toAmazonCoverUrl
import io.github.alessandrojean.toshokan.util.toIsbn10
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.http.HttpMethod
import okhttp3.internal.closeQuietly
import javax.inject.Inject

class AmazonCoverProvider @Inject constructor(
  override val client: HttpClient
) : CoverProvider() {

  override val website = CoverProviderWebsite.AMAZON

  override val condition: (SimpleBookInfo) -> Boolean = { book -> book.code.isValidIsbn() }

  override val baseUrl = AMAZON_IMAGES_URL

  override fun findRequest(book: SimpleBookInfo): HttpRequestBuilder = HttpRequestBuilder().apply {
    method = HttpMethod.Get
    url(book.code.toAmazonCoverUrl()!!)
  }

  override suspend fun findParse(response: HttpResponse): List<BookCover.Result> {
    val bodyStream = response.body<ByteArray>().inputStream()
    val image = BitmapFactory.decodeStream(bodyStream)
    val width = image.width
    val height = image.height

    bodyStream.closeQuietly()

    // If Amazon doesn't have the image, it just returns a 1x1 empty image
    // instead of throwing a HTTP 404 error.
    if (width == 1 && height == 1) {
      return emptyList()
    }

    return listOf(
      BookCover.Result(
        source = R.string.amazon,
        imageUrl = response.request.url.toString()
      )
    )
  }

  companion object {
    private const val AMAZON_IMAGES_URL = "https://images-na.ssl-images-amazon.com/images/P"
    private const val AMAZON_IMAGE_FULL_SIZE = "SCRM"
    private const val AMAZON_IMAGE_SIZE = "SL700"

    fun isbnToAmazonCoverUrl(isbn: String): String? {
      if (!isbn.isValidIsbn()) {
        return null
      }

      val arguments = listOf(AMAZON_IMAGE_FULL_SIZE, AMAZON_IMAGE_SIZE)
        .filter { it.isNotEmpty() }

      val argumentsUrl = if (arguments.isNotEmpty()) {
        arguments.joinToString("_", prefix = "._", postfix = "_")
      } else {
        ""
      }

      return "$AMAZON_IMAGES_URL/${isbn.toIsbn10()}.01$argumentsUrl.jpg"
    }
  }

}