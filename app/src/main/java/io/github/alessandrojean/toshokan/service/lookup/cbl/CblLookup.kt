package io.github.alessandrojean.toshokan.service.lookup.cbl

import io.github.alessandrojean.toshokan.domain.CreditRole
import io.github.alessandrojean.toshokan.service.lookup.LookupBookContributor
import io.github.alessandrojean.toshokan.service.lookup.LookupBookResult
import io.github.alessandrojean.toshokan.service.lookup.LookupProvider
import io.github.alessandrojean.toshokan.service.lookup.Provider
import io.github.alessandrojean.toshokan.util.toIsbnInformation
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import javax.inject.Inject

class CblLookup @Inject constructor (
  override val client: HttpClient
): LookupProvider() {

  override val baseUrl = "https://isbn-search-br.search.windows.net/indexes/isbn-index/docs"

  override val provider = Provider.CBL

  override fun headersBuilder(): HeadersBuilder = HeadersBuilder().apply {
    append(HttpHeaders.Accept, ContentType.Application.Json.toString())
    append("Api-Key", CBL_API_KEY)
    append(HttpHeaders.Origin, CBL_SITE_URL)
    append(HttpHeaders.Referrer, "$CBL_SITE_URL/")
    append(HttpHeaders.UserAgent, USER_AGENT)
  }

  override suspend fun searchByIsbn(isbn: String): List<LookupBookResult> {
    if (isbn.toIsbnInformation()?.country != "BR") {
      return emptyList()
    }

    return super.searchByIsbn(isbn)
  }

  override fun searchRequest(isbn: String): HttpRequestBuilder = HttpRequestBuilder().apply {
    method = HttpMethod.Post
    url("$baseUrl/search?api-version=$CBL_API_VERSION")
    headers.appendAll(this@CblLookup.headers)
    contentType(ContentType.Application.Json)
    setBody(
      CblSearchRequest(
        count = true,
        facets = listOf("Imprint,count:50", "Authors,count:50"),
        filter = "",
        orderBy = null,
        queryType = "full",
        search = isbn,
        searchFields = "FormattedKey,RowKey",
        searchMode = "any",
        select = FIELDS_TO_SELECT.joinToString(","),
        skip = 0,
        top = 12
      )
    )
  }

  override suspend fun searchParse(response: HttpResponse): List<LookupBookResult> {
    val result = response.body<CblSearchResult>()

    if (result.count == 0) {
      return emptyList()
    }

    return result.value.map { record -> record.toLookupBookResult() }
  }

  private fun CblRecord.toLookupBookResult(): LookupBookResult = LookupBookResult(
    isbn = rowKey,
    title = title.trim()
      .replace(TITLE_FIX_REGEX, " #$1")
      .replace(TITLE_VOLUME_REGEX, "#0$1"),
    contributors = if (roles.orEmpty().size != authors.size) {
      authors.map { LookupBookContributor(it, CreditRole.AUTHOR) }
    } else {
      authors.zip(roles!!)
        .map { (author, role) -> LookupBookContributor(author, role.toCreditRole()) }
    },
    publisher = PUBLISHER_REPLACEMENTS[publisher] ?: publisher,
    dimensions = dimensions
      ?.let { dimension ->
        val match = DIMENSION_REGEX.find(dimension)
          ?: return@let emptyList()

        listOf(
          (match.groupValues[1] + "." + match.groupValues[2].ifEmpty{ "0" }).toFloatOrNull() ?: 0f,
          (match.groupValues[3] + "." + match.groupValues[4].ifEmpty { "0" }).toFloatOrNull() ?: 0f
        )
      } ?: emptyList(),
    synopsis = synopsis.orEmpty().trim(),
    pageCount = pageCount?.toIntOrNull() ?: 0,
    providerId = id.orEmpty()
  )

  private fun String.toCreditRole(): CreditRole {
    return CONTRIBUTION_MAPPING.getOrDefault(this, CreditRole.UNKNOWN)
  }

  companion object {
    private const val CBL_API_KEY = "100216A23C5AEE390338BBD19EA86D29"
    private const val CBL_API_VERSION = "2016-09-01"
    private val CBL_SITE_URL = Provider.CBL.url

    private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
      "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.67 Safari/537.36"
    
    private val FIELDS_TO_SELECT = listOf(
      "Authors",
      "Colection",
      "Countries",
      "Date",
      "Imprint",
      "Title",
      "RowKey",
      "PartitionKey",
      "RecordId",
      "FormattedKey",
      "Subject",
      "Veiculacao",
      "Profissoes",
      "Dimensao",
      "Sinopse"
    )

    private val CONTRIBUTION_MAPPING = mapOf(
      "Autor" to CreditRole.AUTHOR,
      "Ilustrador" to CreditRole.ILLUSTRATOR,
      "Roteirista" to CreditRole.SCRIPT,
      "Editor" to CreditRole.EDITOR,
      "Tradutor" to CreditRole.TRANSLATOR,
      "Diagramador" to CreditRole.GRAPHIC_DESIGN
    )
    private val PUBLISHER_REPLACEMENTS = mapOf(
      "Editora JBC" to "JBC",
      "INK" to "JBC",
      "Japorama Editora e Comunicação" to "JBC",
      "New Pop Editora" to "NewPOP",
      "NewPOP Editora" to "NewPOP",
      "Panini Brasil" to "Panini",
      "Panini Comics" to "Panini",
      "Bernardo Ferreira de Santana Carvalho" to "Panini",
      "CONRAD" to "Conrad",
      "Editora Alto Astral" to "Alto Astral",
      "Editora Draco" to "Draco",
      "L&PM Editores" to "L&PM",
      "Pipoca e Nanquim" to "Pipoca & Nanquim",
      "Pipoca e Nanquim Editora LTDA" to "Pipoca & Nanquim",
      "Darkside Books" to "DarkSide",
      "Kleber de Sousa" to "Devir",
      "Verus Editora" to "Verus",
      "reginaldo f silva" to "ComixZone"
    )

    private val TITLE_FIX_REGEX = "(?::| -)? ?(?:v|vol|volume)?(?:\\.|:)? ?(\\d+)$"
      .toRegex(RegexOption.IGNORE_CASE)
    private val TITLE_VOLUME_REGEX = "#(\\d)$".toRegex()
    private val DIMENSION_REGEX = "(\\d{2})(\\d)?x(\\d{2})(\\d)?$".toRegex()
  }
}
