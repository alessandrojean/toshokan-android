package io.github.alessandrojean.toshokan.service.lookup.openlibrary

import io.github.alessandrojean.toshokan.network.GET
import io.github.alessandrojean.toshokan.network.parseAs
import io.github.alessandrojean.toshokan.service.lookup.Provider
import io.github.alessandrojean.toshokan.service.lookup.LookupBookResult
import io.github.alessandrojean.toshokan.service.lookup.LookupProvider
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response

class OpenLibraryLookup : LookupProvider() {

  override val name = "Open Library"

  override val baseUrl = "https://openlibrary.org"

  override val provider = Provider.OPEN_LIBRARY

  override fun headersBuilder(): Headers.Builder = Headers.Builder()
    .add("Accept", "application/json")
    .add("User-Agent", "Toshokan " + System.getProperty("http.agent"))

  override fun searchRequest(isbn: String): Request {
    val requestUrl = "$baseUrl/api/books".toHttpUrl().newBuilder()
      .addQueryParameter("bibkeys", "ISBN:$isbn")
      .addQueryParameter("jscmd", "data")
      .addQueryParameter("format", "json")
      .build()

    return GET(requestUrl.toString(), headers)
  }

  override fun searchParse(response: Response): List<LookupBookResult> {
    val result = response.parseAs<OpenLibraryResult>()
    val bibKey = response.request.url.queryParameter("bibkeys")!!

    if (!result.containsKey(bibKey)) {
      return emptyList()
    }

    return listOf(result[bibKey]!!.toLookupBookResult())
  }

  private fun OpenLibraryBook.toLookupBookResult(): LookupBookResult = LookupBookResult(
    isbn = identifiers["isbn_13"]?.get(0) ?: identifiers["isbn_10"]!![0],
    title = title,
    authors = authors.orEmpty().map { it.name },
    publisher = publishers[0].name,
    coverUrl = cover?.large.orEmpty(),
    providerId = url
      .substringAfter("books/")
      .substringBefore("/"),
  )
}
