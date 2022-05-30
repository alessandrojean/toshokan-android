package io.github.alessandrojean.toshokan.service.cover.wordpress

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.alessandrojean.toshokan.service.cover.CoverProvider
import io.github.alessandrojean.toshokan.service.cover.CoverProviderWebsite
import io.github.alessandrojean.toshokan.service.cover.CoverResult
import io.github.alessandrojean.toshokan.service.cover.SimpleBookInfo
import io.github.alessandrojean.toshokan.util.removeDashes
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod

class WordPressCoverProvider @AssistedInject constructor(
  override val client: HttpClient,
  @Assisted("website") override val website: CoverProviderWebsite,
  @Assisted("condition") override val condition: (SimpleBookInfo) -> Boolean,
  @Assisted("baseUrl") override val baseUrl: String,
  @Assisted("collection") private val collection: String = COLLECTION_POSTS,
  @Assisted("queryParameter") private val queryParameter: String = "search",
  @Assisted("transformer") private val transformer: (SimpleBookInfo) -> String = CODE_TRANSFORMER
) : CoverProvider() {

  @AssistedFactory
  interface Factory {
    fun create(
      @Assisted("website") website: CoverProviderWebsite,
      @Assisted("condition") condition: (SimpleBookInfo) -> Boolean,
      @Assisted("baseUrl") baseUrl: String,
      @Assisted("collection") collection: String = COLLECTION_POSTS,
      @Assisted("queryParameter") queryParameter: String = "search",
      @Assisted("transformer") transformer: (SimpleBookInfo) -> String = CODE_TRANSFORMER
    ): WordPressCoverProvider
  }

  override fun headersBuilder(): HeadersBuilder = HeadersBuilder().apply {
    append(HttpHeaders.Accept, ContentType.Application.Json.toString())
  }

  override fun findRequest(book: SimpleBookInfo): HttpRequestBuilder = HttpRequestBuilder().apply {
    method = HttpMethod.Get
    url("$baseUrl/wp-json/wp/v2/$collection")
    headers.appendAll(this@WordPressCoverProvider.headers)

    url {
      parameters.append("_embed", "wp:featuredmedia")
      parameters.append(queryParameter, transformer(book))
    }
  }

  override suspend fun findParse(response: HttpResponse): List<CoverResult> {
    val result = response.body<List<WordPressItem>>()

    if (result.isEmpty()) {
      return emptyList()
    }

    return result
      .flatMap { it.embedded?.featuredMedia.orEmpty() }
      .mapNotNull { it.sourceUrl }
      .map { sourceUrl -> CoverResult(imageUrl = sourceUrl) }
  }

  companion object {
    const val COLLECTION_POSTS = "posts"
    const val COLLECTION_PROJECTS = "project"
    const val COLLECTION_PRODUCTS = "product"

    val CODE_TRANSFORMER: (SimpleBookInfo) -> String = { it.code.removeDashes() }
    val TITLE_TRANSFORMER: (SimpleBookInfo) -> String = SimpleBookInfo::title
  }

}