package io.github.alessandrojean.toshokan.service.cover.oembed

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.alessandrojean.toshokan.service.cover.CoverProvider
import io.github.alessandrojean.toshokan.service.cover.CoverProviderWebsite
import io.github.alessandrojean.toshokan.service.cover.CoverResult
import io.github.alessandrojean.toshokan.service.cover.SimpleBookInfo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod

class OembedCoverProvider @AssistedInject constructor(
  override val client: HttpClient,
  @Assisted("website") override val website: CoverProviderWebsite,
  @Assisted("condition") override val condition: (SimpleBookInfo) -> Boolean,
  @Assisted("baseUrl") override val baseUrl: String,
  @Assisted("createPath") private val createPath: (SimpleBookInfo) -> String
) : CoverProvider() {

  @AssistedFactory
  interface Factory {
    fun create(
      @Assisted("website") website: CoverProviderWebsite,
      @Assisted("condition") condition: (SimpleBookInfo) -> Boolean,
      @Assisted("baseUrl") baseUrl: String,
      @Assisted("createPath") createPath: (SimpleBookInfo) -> String
    ): OembedCoverProvider
  }

  override fun headersBuilder(): HeadersBuilder = HeadersBuilder().apply {
    append(HttpHeaders.Accept, ContentType.Application.Json.toString())
  }

  override fun findRequest(book: SimpleBookInfo): HttpRequestBuilder = HttpRequestBuilder().apply {
    method = HttpMethod.Get
    url("$baseUrl/wp-json/oembed/1.0/embed")

    url {
      parameters.append("url", baseUrl + createPath(book))
    }
  }

  override suspend fun findParse(response: HttpResponse): List<CoverResult> {
    val result = response.body<OembedInformation>()

    if (result.thumbnailUrl.isNullOrBlank()) {
      return emptyList()
    }

    return listOf(
      CoverResult(imageUrl = result.thumbnailUrl)
    )
  }
}
