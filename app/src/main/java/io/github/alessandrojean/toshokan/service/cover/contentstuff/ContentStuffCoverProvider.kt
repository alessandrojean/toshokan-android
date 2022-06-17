package io.github.alessandrojean.toshokan.service.cover.contentstuff

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.alessandrojean.toshokan.service.cover.BookCover
import io.github.alessandrojean.toshokan.service.cover.CoverProvider
import io.github.alessandrojean.toshokan.service.cover.CoverProviderWebsite
import io.github.alessandrojean.toshokan.service.cover.CoverResult
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

class ContentStuffCoverProvider @AssistedInject constructor(
  override val client: HttpClient,
  @Assisted("website") override val website: CoverProviderWebsite,
  @Assisted("condition") override val condition: (SimpleBookInfo) -> Boolean,
  @Assisted("baseUrl") override val baseUrl: String
) : CoverProvider() {

  @AssistedFactory
  interface Factory {
    fun create(
      @Assisted("website") website: CoverProviderWebsite,
      @Assisted("condition") condition: (SimpleBookInfo) -> Boolean,
      @Assisted("baseUrl") baseUrl: String
    ): ContentStuffCoverProvider
  }

  override fun headersBuilder(): HeadersBuilder = HeadersBuilder().apply {
    append(HttpHeaders.Accept, ContentType.Text.Html.toString())
  }

  override fun findRequest(book: SimpleBookInfo): HttpRequestBuilder = HttpRequestBuilder().apply {
    method = HttpMethod.Get
    url("$baseUrl/busca.aspx")

    url {
      parameters.append("t", book.code.removeDashes())
    }
  }

  override suspend fun findParse(response: HttpResponse): List<BookCover.Result> {
    val document = response.document()
    val firstProductImage = document.selectFirst("#content div.product div.image img.img-responsive")
      ?: return emptyList()

    val coverUrl = firstProductImage.attr("src")
      .replace(IMAGE_SIZE_REGEX) { "/img/${it.groupValues[1].toInt() + 2}_900x900." }

    return listOf(BookCover.Result(source = website.title, imageUrl = coverUrl))
  }

  companion object {
    private val IMAGE_SIZE_REGEX = "/img/(\\d+)_(\\d+)x(\\d+)\\.".toRegex()
  }

}