package io.github.alessandrojean.toshokan.service.lookup.skoob

import io.github.alessandrojean.toshokan.domain.CreditRole
import io.github.alessandrojean.toshokan.service.lookup.LookupBookContributor
import io.github.alessandrojean.toshokan.service.lookup.LookupBookResult
import io.github.alessandrojean.toshokan.service.lookup.LookupProvider
import io.github.alessandrojean.toshokan.service.lookup.Provider
import io.github.alessandrojean.toshokan.util.extension.getRequest
import io.github.alessandrojean.toshokan.util.extension.urlWithBuilder
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.appendPathSegments
import javax.inject.Inject

class SkoobLookup @Inject constructor(
  override val client: HttpClient
) : LookupProvider() {

  override val baseUrl = "https://api.skoob.com.br/api2"

  override val provider = Provider.SKOOB

  override fun headersBuilder(): HeadersBuilder = HeadersBuilder().apply {
    append(HttpHeaders.Accept, ContentType.Application.Json.toString())
    append(HttpHeaders.UserAgent, USER_AGENT)
  }

  override fun searchRequest(isbn: String) = getRequest {
    urlWithBuilder("$baseUrl/book/search") {
      appendPathSegments(
        "term:" + isbn.replace("-", ""),
        "limit:8",
        "page:1",
        "isbn:true",
        "ranking:true"
      )
    }
  }

  override suspend fun searchParse(response: HttpResponse): List<LookupBookResult> {
    val result = response.body<SkoobResponse<List<SkoobBook>>>()

    if (!result.success) {
      return emptyList()
    }

    return result.response.orEmpty()
      .distinctBy(SkoobBook::bookId)
      .map { book -> book.toLookupBookResult() }
  }

  private fun SkoobBook.toLookupBookResult(): LookupBookResult = LookupBookResult(
    isbn = isbn.toString(),
    title = title.orEmpty(),
    contributors = author.orEmpty()
      .split(",", "&")
      .map { LookupBookContributor(it.trim(), CreditRole.AUTHOR) },
    publisher = publisher.orEmpty(),
    coverUrl = coverUrl.orEmpty(),
    synopsis = synopsis.orEmpty(),
    pageCount = pageCount ?: 0,
    providerId = bookId.toString()
  )

  companion object {
    private const val USER_AGENT = "okhttp/3.12.12"
  }

}