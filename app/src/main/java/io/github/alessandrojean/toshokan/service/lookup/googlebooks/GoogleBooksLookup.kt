package io.github.alessandrojean.toshokan.service.lookup.googlebooks

import io.github.alessandrojean.toshokan.network.GET
import io.github.alessandrojean.toshokan.network.parseAs
import io.github.alessandrojean.toshokan.service.lookup.Provider
import io.github.alessandrojean.toshokan.service.lookup.LookupBookResult
import io.github.alessandrojean.toshokan.service.lookup.LookupProvider
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

class GoogleBooksLookup @Inject constructor (
  override val client: OkHttpClient
) : LookupProvider() {
  override val name = "Google Books"

  override val baseUrl = "https://www.googleapis.com/books/v1"

  override val provider = Provider.GOOGLE_BOOKS

  override fun headersBuilder(): Headers.Builder = Headers.Builder()
    .add("Accept", "application/json")
    .add("User-Agent", "Toshokan " + System.getProperty("http.agent"))

  override fun searchRequest(isbn: String): Request {
    val requestUrl = "$baseUrl/volumes".toHttpUrl().newBuilder()
      .addQueryParameter("q", "isbn:" + isbn.replace("-", ""))
      .build()

    return GET(requestUrl.toString(), headers)
  }

  override fun searchParse(response: Response): List<LookupBookResult> {
    val result = response.parseAs<GoogleBooksResult>()

    if (result.items.isNullOrEmpty()) {
      return emptyList()
    }

    return result.items.map { volume -> volume.toLookupBookResult() }
  }

  private fun GoogleBooksVolume.toLookupBookResult(): LookupBookResult = LookupBookResult(
    isbn = volumeInfo.industryIdentifiers
      .filter { it.type.contains("ISBN") }
      .minByOrNull { it.type }!!
      .identifier,
    title = volumeInfo.title,
    authors = volumeInfo.authors.orEmpty(),
    publisher = volumeInfo.publisher.orEmpty(),
    synopsis = volumeInfo.description.orEmpty(),
    dimensions = if (volumeInfo.dimensions.isValid) {
      listOf(
        volumeInfo.dimensions!!.width.replace(" cm", "").toFloatOrNull() ?: 0f,
        volumeInfo.dimensions.height.replace(" cm", "").toFloatOrNull() ?: 0f
      )
    } else {
      emptyList()
    },
    providerId = id,
  )

  private val GoogleBooksDimensions?.isValid
    get() = this != null && width.contains("cm") && height.contains("cm")
}
