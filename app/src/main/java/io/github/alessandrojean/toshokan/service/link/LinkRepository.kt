package io.github.alessandrojean.toshokan.service.link

import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.CompleteBook
import io.github.alessandrojean.toshokan.service.link.LinkProvider.Companion.ISBN_10
import io.github.alessandrojean.toshokan.service.link.LinkProvider.Companion.ISBN_13
import io.github.alessandrojean.toshokan.util.toIsbnInformation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LinkRepository @Inject constructor() {

  private val providers = listOf(
    /* Database websites */
    LinkProvider(
      name = R.string.goodreads,
      icon = R.drawable.ic_goodreads_logo,
      category = LinkCategory.DATABASE,
      urlWithPlaceholder = "https://goodreads.com/search?q=$ISBN_13"
    ),
    LinkProvider(
      name = R.string.open_library,
      category = LinkCategory.DATABASE,
      urlWithPlaceholder = "https://openlibrary.org/isbn/$ISBN_13"
    ),
    LinkProvider(
      name = R.string.skoob,
      icon = R.drawable.ic_skoob_logo,
      category = LinkCategory.DATABASE,
      urlWithPlaceholder = "https://www.skoob.com.br/livro/lista/busca:$ISBN_13/tipo:isbn"
    ),
    /* Stores */
    LinkProvider(
      name = R.string.amazon,
      icon = R.drawable.ic_amazon_logo,
      category = LinkCategory.STORE,
      condition = { it.code?.toIsbnInformation()?.country == "US" },
      urlWithPlaceholder = "https://www.amazon.com/dp/$ISBN_10"
    ),
    LinkProvider(
      name = R.string.amazon_jp,
      icon = R.drawable.ic_amazon_logo,
      category = LinkCategory.STORE,
      condition = { it.code?.toIsbnInformation()?.country == "JP" },
      urlWithPlaceholder = "https://www.amazon.co.jp/dp/$ISBN_10"
    ),
    LinkProvider(
      name = R.string.amazon_br,
      icon = R.drawable.ic_amazon_logo,
      category = LinkCategory.STORE,
      condition = { it.code?.toIsbnInformation()?.country == "BR" },
      urlWithPlaceholder = "https://www.amazon.com.br/dp/$ISBN_10"
    ),
    LinkProvider(
      name = R.string.amazon_es,
      icon = R.drawable.ic_amazon_logo,
      category = LinkCategory.STORE,
      condition = { it.code?.toIsbnInformation()?.country == "ES" },
      urlWithPlaceholder = "https://www.amazon.es/dp/$ISBN_10"
    ),
    LinkProvider(
      name = R.string.loja_panini,
      category = LinkCategory.STORE,
      condition = { book ->
        book.code?.toIsbnInformation()?.country == "BR" &&
          book.publisher_name.contains("Panini", ignoreCase = true)
      },
      urlWithPlaceholder = "https://loja.panini.com.br/panini/solucoes/busca.aspx?t=$ISBN_13"
    ),
    LinkProvider(
      name = R.string.newpop_shop,
      category = LinkCategory.STORE,
      condition = { book ->
        book.code?.toIsbnInformation()?.country == "BR" &&
          book.publisher_name.contains("NewPOP", ignoreCase = true)
      },
      urlWithPlaceholder = "https://www.lojanewpop.com.br/buscar?q=$ISBN_13"
    ),
    LinkProvider(
      name = R.string.fnac_pt,
      category = LinkCategory.STORE,
      condition = { it.code?.toIsbnInformation()?.country == "PT" },
      urlWithPlaceholder = "https://fnac.pt/SearchResult/ResultList.aspx?Search=$ISBN_13"
    )
  )

  fun generateBookLinks(book: CompleteBook): List<BookLink> {
    return providers.mapNotNull { it.generateUrl(book) }
  }

}