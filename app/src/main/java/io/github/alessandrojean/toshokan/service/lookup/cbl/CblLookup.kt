package io.github.alessandrojean.toshokan.service.lookup.cbl

import io.github.alessandrojean.toshokan.network.POST
import io.github.alessandrojean.toshokan.network.parseAs
import io.github.alessandrojean.toshokan.service.lookup.Provider
import io.github.alessandrojean.toshokan.service.lookup.LookupBookResult
import io.github.alessandrojean.toshokan.service.lookup.LookupProvider
import io.github.alessandrojean.toshokan.util.toAmazonCoverUrl
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class CblLookup : LookupProvider() {

  override val name = "CBL"

  override val baseUrl = "https://isbn-search-br.search.windows.net/indexes/isbn-index/docs"

  override val provider = Provider.CBL

  override fun headersBuilder(): Headers.Builder = Headers.Builder()
    .add("Accept", "application/json")
    .add("Api-Key", CBL_API_KEY)
    .add("Origin", CBL_SITE_URL)
    .add("Referer", "$CBL_SITE_URL/")
    .add("User-Agent", USER_AGENT)

  override fun searchRequest(isbn: String): Request {
    val jsonPayload = buildJsonObject {
      put("count", true)
      putJsonArray("facets") {
        add("Imprint,count:50")
        add("Authors,count:50")
      }
      put("filter", "")
      put("orderby", null as String?)
      put("queryType", "full")
      put("search", isbn)
      put("searchFields", "FormattedKey,RowKey")
      put("searchMode", "any")
      put("select", FIELDS_TO_SELECT.joinToString(","))
      put("skip", 0)
      put("top", 12)
    }

    val requestBody = jsonPayload.toString().toRequestBody(JSON_MEDIA_TYPE)

    val requestHeaders = headersBuilder()
      .add("Content-Length", requestBody.contentLength().toString())
      .add("Content-Type", requestBody.contentType().toString())
      .build()

    return POST("$baseUrl/search?api-version=$CBL_API_VERSION", requestHeaders, requestBody)
  }

  override fun searchParse(response: Response): List<LookupBookResult> {
    val result = response.parseAs<CblSearchResult>()

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
    authors = if (roles.orEmpty().size >= authors.size) {
      authors.filterIndexed { i, _ -> ALLOWED_ROLES.contains(roles!![i]) }
    } else {
      authors
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
    synopsis = synopsis.orEmpty(),
    providerId = id.orEmpty()
  )

  companion object {
    private const val CBL_API_KEY = "100216A23C5AEE390338BBD19EA86D29"
    private const val CBL_API_VERSION = "2016-09-01"
    private const val CBL_SITE_URL = "https://www.cblservicos.org.br"

    private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
      "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.67 Safari/537.36"

    private val JSON_MEDIA_TYPE = "application/json".toMediaType()
    
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

    private val ALLOWED_ROLES = listOf("Autor", "Ilustrador", "Roteirista")
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
