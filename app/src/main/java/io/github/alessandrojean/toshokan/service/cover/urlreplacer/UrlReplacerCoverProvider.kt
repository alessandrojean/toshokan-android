package io.github.alessandrojean.toshokan.service.cover.urlreplacer

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.alessandrojean.toshokan.service.cover.BookCover
import io.github.alessandrojean.toshokan.service.cover.CoverProvider
import io.github.alessandrojean.toshokan.service.cover.CoverProviderWebsite
import io.github.alessandrojean.toshokan.service.cover.CoverResult
import io.github.alessandrojean.toshokan.service.cover.SimpleBookInfo
import io.github.alessandrojean.toshokan.util.removeDashes
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse

class UrlReplacerCoverProvider @AssistedInject constructor(
  override val client: HttpClient,
  @Assisted("website") override val website: CoverProviderWebsite,
  @Assisted("condition") override val condition: (SimpleBookInfo) -> Boolean,
  @Assisted("baseUrl") override val baseUrl: String,
  @Assisted("transformer") private val transformer: (SimpleBookInfo) -> String = { it.code.removeDashes() },
) : CoverProvider() {

  @AssistedFactory
  interface Factory {
    fun create(
      @Assisted("website") website: CoverProviderWebsite,
      @Assisted("condition") condition: (SimpleBookInfo) -> Boolean,
      @Assisted("baseUrl") baseUrl: String,
      @Assisted("transformer") transformer: (SimpleBookInfo) -> String = { it.code.removeDashes() }
    ): UrlReplacerCoverProvider
  }

  override suspend fun find(book: SimpleBookInfo): List<BookCover.Result> {
    if (!condition.invoke(book)) {
      return emptyList()
    }

    val valueToReplace = transformer(book)

    return listOf(
      BookCover.Result(
        source = website.title,
        imageUrl = baseUrl.replace(PROPERTY_PLACEHOLDER, valueToReplace)
      )
    )
  }

  override fun findRequest(book: SimpleBookInfo): HttpRequestBuilder =
    throw UnsupportedOperationException()

  override suspend fun findParse(response: HttpResponse): List<BookCover.Result> =
    throw UnsupportedOperationException()

  companion object {
    const val PROPERTY_PLACEHOLDER = "{property}"
  }

}