package io.github.alessandrojean.toshokan.service.cover

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HeadersBuilder

abstract class CoverProvider {

  /**
   * Provider name.
   */
  abstract val website: CoverProviderWebsite

  /**
   * Condition to check if a book is from this provider.
   */
  abstract val condition: (SimpleBookInfo) -> Boolean

  /**
   * Base URL of the provider API.
   */
  protected abstract val baseUrl: String

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
   * Find the covers for the book.
   */
  open suspend fun find(book: SimpleBookInfo): List<CoverResult> {
    if (!condition.invoke(book)) {
      return emptyList()
    }

    val result = runCatching {
      val request = findRequest(book)
      val response = client.request(request)

      findParse(response)
        .map { it.copy(source = website.title) }
    }

    if (result.isFailure) {
      result.exceptionOrNull()?.printStackTrace()
    }

    return result.getOrNull() ?: emptyList()
  }

  /**
   * Create the find request to be used in the API call.
   */
  protected abstract fun findRequest(book: SimpleBookInfo): HttpRequestBuilder

  /**
   * Parse the response and convert it to the proper class.
   */
  protected abstract suspend fun findParse(response: HttpResponse): List<CoverResult>

}