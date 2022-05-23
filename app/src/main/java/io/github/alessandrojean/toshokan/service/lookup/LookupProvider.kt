package io.github.alessandrojean.toshokan.service.lookup

import io.github.alessandrojean.toshokan.network.awaitSuccess
import io.github.alessandrojean.toshokan.util.isValidIsbn
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

abstract class LookupProvider {

  /**
   * Provider name.
   */
  abstract val name: String

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
  protected abstract val client: OkHttpClient

  /**
   * Create the headers to be used in the requests.
   */
  protected open fun headersBuilder(): Headers.Builder = Headers.Builder()

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
      val request = searchRequest(isbn.replace("-", ""))
      val response = client.newCall(request).awaitSuccess()

      searchParse(response)
        .map { it.copy(provider = provider) }
    }

    return result.getOrNull() ?: emptyList()
  }

  /**
   * Create the search request to be used in the API call.
   */
  protected abstract fun searchRequest(isbn: String): Request

  /**
   * Parse the response and convert it to the proper class.
   */
  protected abstract fun searchParse(response: Response): List<LookupBookResult>
}
