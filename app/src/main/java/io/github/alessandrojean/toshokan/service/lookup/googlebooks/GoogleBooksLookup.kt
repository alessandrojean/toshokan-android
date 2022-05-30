package io.github.alessandrojean.toshokan.service.lookup.googlebooks

import io.github.alessandrojean.toshokan.domain.CreditRole
import io.github.alessandrojean.toshokan.service.lookup.LookupBookContributor
import io.github.alessandrojean.toshokan.service.lookup.Provider
import io.github.alessandrojean.toshokan.service.lookup.LookupBookResult
import io.github.alessandrojean.toshokan.service.lookup.LookupProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import javax.inject.Inject

class GoogleBooksLookup @Inject constructor (
  override val client: HttpClient
) : LookupProvider() {

  override val baseUrl = "https://www.googleapis.com/books/v1"

  override val provider = Provider.GOOGLE_BOOKS

  override fun headersBuilder(): HeadersBuilder = HeadersBuilder().apply {
    append(HttpHeaders.Accept, ContentType.Application.Json.toString())
    append(HttpHeaders.UserAgent, "Toshokan " + System.getProperty("http.agent"))
  }

  override fun searchRequest(isbn: String): HttpRequestBuilder = HttpRequestBuilder().apply {
    method = HttpMethod.Get
    url("$baseUrl/volumes")

    url {
      parameters.append("q", "isbn:" + isbn.replace("-", ""))
    }
  }

  override suspend fun searchParse(response: HttpResponse): List<LookupBookResult> {
    val result = response.body<GoogleBooksResult>()

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
    contributors = volumeInfo.authors.orEmpty()
      .map { LookupBookContributor(it, CreditRole.AUTHOR) },
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
