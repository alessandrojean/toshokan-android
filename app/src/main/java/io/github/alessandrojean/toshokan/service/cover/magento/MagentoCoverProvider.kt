package io.github.alessandrojean.toshokan.service.cover.magento

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.alessandrojean.toshokan.service.cover.BookCover
import io.github.alessandrojean.toshokan.service.cover.CoverProvider
import io.github.alessandrojean.toshokan.service.cover.CoverProviderWebsite
import io.github.alessandrojean.toshokan.service.cover.SimpleBookInfo
import io.github.alessandrojean.toshokan.util.extension.document
import io.github.alessandrojean.toshokan.util.removeDashes
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class MagentoCoverProvider @AssistedInject constructor(
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
    ): MagentoCoverProvider
  }

  override fun headersBuilder(): HeadersBuilder = HeadersBuilder().apply {
    append(HttpHeaders.Accept, ContentType.Text.Html.toString())
  }

  override fun findRequest(book: SimpleBookInfo): HttpRequestBuilder = HttpRequestBuilder().apply {
    method = HttpMethod.Get
    url("$baseUrl/${createPath(book)}")
  }

  override suspend fun findParse(response: HttpResponse): List<BookCover.Result> {
    val document = response.document()
    val productImage = document.selectFirst("meta[property=og:image]")
      ?: return emptyList()

    val imageUrl = productImage.attr("content")
      .takeIf { !it.contains("placeholder") } ?: return emptyList()

    val fullImageUrl = imageUrl.replace(CACHE_PATH_REGEX, "/")

    return listOf(BookCover.Result(source = website.title, imageUrl = fullImageUrl))
  }

  companion object {
    private val CACHE_PATH_REGEX = "/cache/[a-f0-9]*/".toRegex()
  }

}