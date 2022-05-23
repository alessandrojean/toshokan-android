package io.github.alessandrojean.toshokan.service.lookup.mercadoeditorial

import io.github.alessandrojean.toshokan.network.GET
import io.github.alessandrojean.toshokan.network.parseAs
import io.github.alessandrojean.toshokan.service.lookup.Provider
import io.github.alessandrojean.toshokan.service.lookup.LookupBookResult
import io.github.alessandrojean.toshokan.service.lookup.LookupProvider
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response

class MercadoEditorialLookup : LookupProvider() {
  override val name = "Mercado Editorial"

  override val baseUrl = "https://api.mercadoeditorial.org/api/v1.2"

  override val provider = Provider.MERCADO_EDITORIAL

  override fun headersBuilder(): Headers.Builder = Headers.Builder()
    .add("Accept", "application/json")
    .add("User-Agent", "Toshokan " + System.getProperty("http.agent"))

  override fun searchRequest(isbn: String): Request {
    val requestUrl = "$baseUrl/book".toHttpUrl().newBuilder()
      .addQueryParameter("isbn", isbn.replace("-", ""))
      .build()

    return GET(requestUrl.toString(), headers)
  }

  override fun searchParse(response: Response): List<LookupBookResult> {
    val result = response.parseAs<MercadoEditorialResult>()

    if (result.books.isNullOrEmpty()) {
      return emptyList()
    }

    return result.books.map { book -> book.toLookupBookResult() }
  }

  private fun MercadoEditorialBook.toLookupBookResult(): LookupBookResult = LookupBookResult(
    isbn = this.isbn,
    title = this.title,
    authors = contributions.orEmpty()
      .filter { it.code in VALID_CONTRIBUTION_CODES }
      .map {
        arrayOf(it.firstName, it.lastName)
          .filterNot(String::isNullOrEmpty)
          .joinToString(" ")
      },
    publisher = publisher?.name.orEmpty(),
    synopsis = this.synopsis.orEmpty(),
    dimensions = if (dimensions.isValid) {
      listOf(dimensions!!.width.toFloat(), dimensions.height.toFloat())
    } else {
      emptyList()
    },
    providerId = isbn,
  )

  private val MercadoEditorialDimension?.isValid
    get() = this != null && width.isNotEmpty() && height.isNotEmpty() &&
      width.toFloatOrNull() != null && height.toFloatOrNull() != null

  companion object {
    private val VALID_CONTRIBUTION_CODES = arrayOf(1, 8, 24)
  }
}
