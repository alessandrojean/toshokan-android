package io.github.alessandrojean.toshokan.service.lookup.openlibrary

import io.github.alessandrojean.toshokan.domain.CreditRole
import io.github.alessandrojean.toshokan.service.lookup.LookupBookContributor
import io.github.alessandrojean.toshokan.service.lookup.Provider
import io.github.alessandrojean.toshokan.service.lookup.LookupBookResult
import io.github.alessandrojean.toshokan.service.lookup.LookupProvider
import io.github.alessandrojean.toshokan.service.lookup.cbl.CblLookup
import io.github.alessandrojean.toshokan.util.removeDashes
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class OpenLibraryLookup @Inject constructor (
  override val client: HttpClient
) : LookupProvider() {

  override val baseUrl = "https://openlibrary.org"

  override val provider = Provider.OPEN_LIBRARY

  override fun headersBuilder(): HeadersBuilder = HeadersBuilder().apply {
    append(HttpHeaders.Accept, ContentType.Application.Json.toString())
    append(HttpHeaders.UserAgent, "Toshokan " + System.getProperty("http.agent"))
  }

  override fun searchRequest(isbn: String): HttpRequestBuilder = HttpRequestBuilder().apply {
    method = HttpMethod.Get
    url("$baseUrl/api/books")
    headers.appendAll(this@OpenLibraryLookup.headers)
    url {
      parameters.append("bibkeys", "ISBN:$isbn")
      parameters.append("jscmd", "data")
      parameters.append("format", "json")
    }
  }

  override suspend fun searchParse(response: HttpResponse): List<LookupBookResult> {
    val result = response.body<OpenLibraryResult>()
    val bibKey = response.request.url.parameters["bibkeys"]!!

    if (!result.containsKey(bibKey)) {
      return emptyList()
    }

    val details = runCatching {
      val detailsRequest = detailsRequest(bibKey.removePrefix("ISBN:"))
      val detailsResponse = client.request(detailsRequest)

      detailsParse(detailsResponse)
    }

    return listOf(result[bibKey]!!.toLookupBookResult(details.getOrNull()))
  }

  private fun detailsRequest(isbn: String): HttpRequestBuilder = HttpRequestBuilder().apply {
    method = HttpMethod.Get
    url("$baseUrl/isbn/${isbn.removeDashes()}.json")
    headers.appendAll(this@OpenLibraryLookup.headers)
  }

  private suspend fun detailsParse(response: HttpResponse): OpenLibraryBookDetails {
    return response.body()
  }

  private fun OpenLibraryBook.toLookupBookResult(details: OpenLibraryBookDetails?): LookupBookResult {
    val dimensions = details?.physicalDimensions
      ?.removeSuffix(" centimeters")
      ?.split(" x ")
      ?.mapNotNull(String::toFloatOrNull)
      .orEmpty()

    return LookupBookResult(
      isbn = identifiers["isbn_13"]?.get(0) ?: identifiers["isbn_10"]!![0],
      title = title,
      contributors = authors.orEmpty().map { LookupBookContributor(it.name, CreditRole.AUTHOR) } +
        details?.contributors.orEmpty().map {
          LookupBookContributor(
            name = it.name,
            role = it.role.orEmpty().toCreditRole()
          )
        },
      publisher = publishers[0].name,
      synopsis = when (details?.description) {
        is JsonObject -> {
          details.description.jsonObject["value"]?.jsonPrimitive?.contentOrNull.orEmpty()
        }
        is JsonPrimitive -> {
          details.description.jsonPrimitive.contentOrNull.orEmpty()
        }
        else -> ""
      },
      dimensions = if (
        details?.physicalDimensions.orEmpty().endsWith(" centimeters") &&
        dimensions.size == 3
      ) {
        listOf(dimensions[1], dimensions[0])
      } else {
        emptyList()
      },
      coverUrl = cover?.large.orEmpty(),
      providerId = url
        .substringAfter("books/")
        .substringBefore("/"),
    )
  }

  private fun String.toCreditRole(): CreditRole {
    return CONTRIBUTION_MAPPING.getOrDefault(this, CreditRole.UNKNOWN)
  }

  companion object {
    private val CONTRIBUTION_MAPPING = mapOf(
      "Cover Design" to CreditRole.COVER_DESIGN,
      "Editor" to CreditRole.EDITOR,
      "Illustrator" to CreditRole.ILLUSTRATOR,
      "Translator" to CreditRole.TRANSLATOR
    )
  }
}
