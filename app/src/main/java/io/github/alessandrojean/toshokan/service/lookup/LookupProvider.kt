package io.github.alessandrojean.toshokan.service.lookup

import io.github.alessandrojean.toshokan.util.isValidIsbn
import io.github.alessandrojean.toshokan.util.removeDashes
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HeadersBuilder

abstract class LookupProvider {

  /**
   * Base URL of the provider API.
   */
  protected abstract val baseUrl: String

  /**
   * The provider Enum used as key.
   */
  abstract val provider: Provider

  /**
   * Client used to do the HTTP calls.
   */
  protected abstract val client: HttpClient

  /**
   * Create the headers to be used in the requests.
   */
  protected open fun headersBuilder(): HeadersBuilder = HeadersBuilder()

  /**
   * Headers to be used in the HTTP requests.
   */
  protected val headers: Headers by lazy { headersBuilder().build() }

  /**
   * Search the provider for books matching the ISBN.
   */
  open suspend fun searchByIsbn(isbn: String): List<LookupBookResult> {
    if (!isbn.isValidIsbn()) {
      return emptyList()
    }

    val result = runCatching {
      val request = searchRequest(isbn.removeDashes())
      val response = client.request(request)

      searchParse(response)
        .map { it.copy(provider = provider) }
    }

    return result.getOrNull() ?: emptyList()
  }

  /**
   * Create the search request to be used in the API call.
   */
  protected abstract fun searchRequest(isbn: String): HttpRequestBuilder

  /**
   * Parse the response and convert it to the proper class.
   */
  protected abstract suspend fun searchParse(response: HttpResponse): List<LookupBookResult>
}
