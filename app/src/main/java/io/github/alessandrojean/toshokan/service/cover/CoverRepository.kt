package io.github.alessandrojean.toshokan.service.cover

import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.data.preference.PreferencesManager
import io.github.alessandrojean.toshokan.service.cover.amazon.AmazonCoverProvider
import io.github.alessandrojean.toshokan.service.cover.contentstuff.ContentStuffCoverProvider
import io.github.alessandrojean.toshokan.service.cover.oembed.OembedCoverProvider
import io.github.alessandrojean.toshokan.service.cover.urlreplacer.UrlReplacerCoverProvider
import io.github.alessandrojean.toshokan.service.cover.wordpress.WordPressCoverProvider
import io.github.alessandrojean.toshokan.util.extension.toSlug
import io.github.alessandrojean.toshokan.util.extension.toTitleParts
import io.github.alessandrojean.toshokan.util.isValidIsbn
import io.github.alessandrojean.toshokan.util.removeDashes
import io.github.alessandrojean.toshokan.util.toAmazonCoverUrl
import io.github.alessandrojean.toshokan.util.toIsbn10
import io.github.alessandrojean.toshokan.util.toIsbnInformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

interface CoverRepository {
  fun hasProvider(book: SimpleBookInfo): Boolean
  suspend fun find(book: SimpleBookInfo): List<BookCover>
}

@Singleton
class CoverRepositoryImpl @Inject constructor(
  private val amazonCoverProvider: AmazonCoverProvider,
  contentStuffCoverProvider: ContentStuffCoverProvider.Factory,
  oembedCoverProviderFactory: OembedCoverProvider.Factory,
  urlReplacerCoverProviderFactory: UrlReplacerCoverProvider.Factory,
  wordPressCoverProviderFactory: WordPressCoverProvider.Factory,
  private val preferencesManager: PreferencesManager
) : CoverRepository {

  private val providers = arrayOf(
    oembedCoverProviderFactory.create(
      website = CoverProviderWebsite.JBC,
      condition = { book ->
        book.code.toIsbnInformation()?.country == "BR" &&
          book.publisher.contains("jbc", ignoreCase = true)
      },
      baseUrl = CoverProviderWebsite.JBC.url,
      createPath = { book ->
        val series = book.title.toTitleParts().title
          .toSlug(Locale("pt", "BR"))
          .replace("especial", "esp")
        val title = book.title.toSlug(Locale("pt", "BR"))
          .replace("especial", "esp")

        "/mangas/colecao/$series/vol/$title"
      }
    ),
    contentStuffCoverProvider.create(
      website = CoverProviderWebsite.LOJA_PANINI,
      condition = { book ->
        book.code.toIsbnInformation()?.country == "BR" &&
          book.publisher.contains("panini", ignoreCase = true)
      },
      baseUrl = "https://loja.panini.com.br/panini/solucoes"
    ),
    wordPressCoverProviderFactory.create(
      website = CoverProviderWebsite.NEWPOP,
      condition = { book ->
        book.code.toIsbnInformation()?.country == "BR" &&
          book.publisher.contains("newpop", ignoreCase = true)
      },
      baseUrl = CoverProviderWebsite.NEWPOP.url
    ),
    wordPressCoverProviderFactory.create(
      website = CoverProviderWebsite.PIPOCA_E_NANQUIM,
      condition = { book ->
        book.code.toIsbnInformation()?.country == "BR" &&
          book.publisher.contains("pipoca & nanquim", ignoreCase = true)
      },
      baseUrl = CoverProviderWebsite.PIPOCA_E_NANQUIM.url,
      transformer = WordPressCoverProvider.TITLE_TRANSFORMER,
      collection = WordPressCoverProvider.COLLECTION_PRODUCTS
    ),
    urlReplacerCoverProviderFactory.create(
      website = CoverProviderWebsite.SHUEISHA,
      condition = { book ->
        book.code.toIsbnInformation()?.country == "JP" &&
          (book.publisher.contains("shueisha", ignoreCase = true) ||
            book.publisher.contains("集英社"))
      },
      baseUrl = "https://dosbg3xlm0x1t.cloudfront.net/images/items/" +
        "${UrlReplacerCoverProvider.PROPERTY_PLACEHOLDER}/1200/" +
        "${UrlReplacerCoverProvider.PROPERTY_PLACEHOLDER}.jpg"
    ),
    wordPressCoverProviderFactory.create(
      website = CoverProviderWebsite.VENETA,
      condition = { book ->
        book.code.toIsbnInformation()?.country == "BR" &&
          book.publisher.contains("veneta", ignoreCase = true)
      },
      baseUrl = CoverProviderWebsite.VENETA.url,
      transformer = WordPressCoverProvider.TITLE_TRANSFORMER,
      collection = WordPressCoverProvider.COLLECTION_PRODUCTS
    ),
    urlReplacerCoverProviderFactory.create(
      website = CoverProviderWebsite.VIZ_MEDIA,
      condition = { book ->
        book.code.toIsbnInformation()?.country == "US" &&
          book.publisher.contains("viz media", ignoreCase = true)
      },
      baseUrl = "https://dw9to29mmj727.cloudfront.net/products/" +
        "${UrlReplacerCoverProvider.PROPERTY_PLACEHOLDER}.jpg",
      transformer = { it.code.removeDashes().toIsbn10()!! }
    ),
  )

  override fun hasProvider(book: SimpleBookInfo): Boolean {
    val disabledProviders = preferencesManager.disabledCoverProviders().get()
    val providersToSearch = providers.filter { it.website.name !in disabledProviders }

    return providersToSearch.firstOrNull { it.condition.invoke(book) } != null
  }

  override suspend fun find(book: SimpleBookInfo): List<BookCover> {
    return withContext(Dispatchers.IO) {
      if (!book.code.isValidIsbn()) {
        return@withContext emptyList()
      }

      val amazonCover = async {
        runCatching { amazonCoverProvider.find(book) }
          .getOrElse { emptyList() }
      }

      val initialResults = amazonCover.await().toMutableList<BookCover.External>()

      if (book.initialCovers.isNotEmpty()) {
        initialResults += book.initialCovers
          .filterIsInstance<BookCover.External>()
          .filter { it.imageUrl.isNotBlank() }
      }

      val disabledProviders = preferencesManager.disabledCoverProviders().get()
      val providersToSearch = providers.filter { it.website.name !in disabledProviders }

      val provider = providersToSearch.firstOrNull { it.condition.invoke(book) }
        ?: return@withContext initialResults.distinctBy { it.imageUrl }

      val coverResult = async {
        runCatching { provider.find(book) }
          .getOrElse { emptyList() }
      }

      (initialResults + coverResult.await()).distinctBy { it.imageUrl }
    }
  }

}