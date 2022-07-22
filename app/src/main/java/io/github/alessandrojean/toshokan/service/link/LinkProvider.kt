package io.github.alessandrojean.toshokan.service.link

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import io.github.alessandrojean.toshokan.database.data.CompleteBook
import io.github.alessandrojean.toshokan.domain.DomainBook
import io.github.alessandrojean.toshokan.util.isValidIsbn
import io.github.alessandrojean.toshokan.util.removeDashes
import io.github.alessandrojean.toshokan.util.toIsbn10

class LinkProvider(
  @StringRes val name: Int,
  private val condition: (DomainBook) -> Boolean = { true },
  @DrawableRes val icon: Int? = null,
  private val urlWithPlaceholder: String,
  val category: LinkCategory = LinkCategory.DATABASE
) {

  fun generateUrl(book: DomainBook): BookLink? {
    if (!book.code.orEmpty().isValidIsbn() || !condition.invoke(book)) {
      return null
    }

    val isbn13 = book.code!!.removeDashes()
    val isbn10 = isbn13.toIsbn10()

    return BookLink(
      name = name,
      icon = icon,
      category = category,
      url = urlWithPlaceholder
        .replace(ISBN_13, isbn13)
        .replace(ISBN_10, isbn10!!)
    )
  }

  companion object {
    const val ISBN_13 = "{isbn13}"
    const val ISBN_10 = "{isbn10}"
  }

}