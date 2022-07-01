package io.github.alessandrojean.toshokan.service.lookup.mercadoeditorial

import android.icu.util.Currency
import io.github.alessandrojean.toshokan.domain.CreditRole
import io.github.alessandrojean.toshokan.domain.Price
import io.github.alessandrojean.toshokan.service.lookup.LookupBookContributor
import io.github.alessandrojean.toshokan.service.lookup.LookupBookResult
import io.github.alessandrojean.toshokan.service.lookup.LookupProvider
import io.github.alessandrojean.toshokan.service.lookup.Provider
import io.github.alessandrojean.toshokan.util.extension.getRequest
import io.github.alessandrojean.toshokan.util.extension.urlWithBuilder
import io.github.alessandrojean.toshokan.util.toIsbnInformation
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

class MercadoEditorialLookup @Inject constructor (
  override val client: HttpClient
) : LookupProvider() {

  override val baseUrl = "https://api.mercadoeditorial.org/api/v1.2"

  override val provider = Provider.MERCADO_EDITORIAL

  override fun headersBuilder(): HeadersBuilder = HeadersBuilder().apply {
    append(HttpHeaders.Accept, ContentType.Application.Json.toString())
    append(HttpHeaders.UserAgent, "Toshokan " + System.getProperty("http.agent"))
  }

  override suspend fun searchByIsbn(isbn: String): List<LookupBookResult> {
    if (isbn.toIsbnInformation()?.country != "BR") {
      return emptyList()
    }

    return super.searchByIsbn(isbn)
  }

  override fun searchRequest(isbn: String) = getRequest {
    urlWithBuilder("$baseUrl/book") {
      parameters.append("isbn", isbn.replace("-", ""))
    }
  }

  override suspend fun searchParse(response: HttpResponse): List<LookupBookResult> {
    val result = response.body<MercadoEditorialResult>()

    if (result.books.isNullOrEmpty()) {
      return emptyList()
    }

    return result.books.map { book -> book.toLookupBookResult() }
  }

  private fun MercadoEditorialBook.toLookupBookResult(): LookupBookResult = LookupBookResult(
    isbn = this.isbn,
    title = this.title,
    contributors = contributions.orEmpty()
      .filter { it.code in CONTRIBUTION_CODES_MAPPING.keys }
      .map {
        LookupBookContributor(
          name = arrayOf(it.firstName, it.lastName)
            .filterNot(String::isNullOrEmpty)
            .joinToString(" "),
          role = CONTRIBUTION_CODES_MAPPING[it.code]!!
        )
      },
    publisher = publisher?.name.orEmpty(),
    synopsis = this.synopsis.orEmpty(),
    pageCount = dimensions?.pageCount?.toIntOrNull() ?: 0,
    dimensions = if (dimensions.isValid) {
      listOf(dimensions!!.width.toFloat(), dimensions.height.toFloat())
    } else {
      emptyList()
    },
    labelPrice = if (price?.toFloatOrNull() != null && !currency.isNullOrBlank()) {
      Price(currency = Currency.getInstance(currency), value = price.toFloat())
    } else {
      null
    },
    providerId = "ME_$isbn",
  )

  private val MercadoEditorialDimension?.isValid
    get() = this != null && width.isNotEmpty() && height.isNotEmpty() &&
      width.toFloatOrNull() != null && height.toFloatOrNull() != null

  companion object {
    private val CONTRIBUTION_CODES_MAPPING = mapOf(
      1 to CreditRole.AUTHOR,
      5 to CreditRole.EDITOR,
      8 to CreditRole.ILLUSTRATOR,
      9 to CreditRole.TRANSLATOR,
      15 to CreditRole.COVER_DESIGN
    )
  }
}
