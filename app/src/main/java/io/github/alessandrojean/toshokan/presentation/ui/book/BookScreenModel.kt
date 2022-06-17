package io.github.alessandrojean.toshokan.presentation.ui.book

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.net.toUri
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import cafe.adriel.voyager.hilt.ScreenModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.alessandrojean.toshokan.BuildConfig
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.data.ImageSaver
import io.github.alessandrojean.toshokan.data.preference.PreferencesManager
import io.github.alessandrojean.toshokan.database.data.CompleteBook
import io.github.alessandrojean.toshokan.domain.BookNeighbors
import io.github.alessandrojean.toshokan.repository.BooksRepository
import io.github.alessandrojean.toshokan.service.link.BookLink
import io.github.alessandrojean.toshokan.service.link.LinkRepository
import io.github.alessandrojean.toshokan.util.extension.appName
import io.github.alessandrojean.toshokan.util.extension.toLocaleCurrencyString
import io.github.alessandrojean.toshokan.util.extension.toShareIntent
import io.github.alessandrojean.toshokan.util.extension.toSlug
import io.github.alessandrojean.toshokan.util.extension.toTitleParts
import io.github.alessandrojean.toshokan.util.extension.toast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class BookScreenModel @AssistedInject constructor(
  @ActivityContext private val context: Context,
  private val booksRepository: BooksRepository,
  preferencesManager: PreferencesManager,
  private val imageSaver: ImageSaver,
  private val linkRepository: LinkRepository,
  @Assisted private val bookId: Long
) : ScreenModel {

  @AssistedFactory
  interface Factory : ScreenModelFactory {
    fun create(@Assisted bookId: Long): BookScreenModel
  }

  val book = booksRepository.findById(bookId)
  val contributors = booksRepository.findBookContributorsAsFlow(bookId)

  val showBookNavigation = preferencesManager.showBookNavigation().asFlow()

  fun findBookLinks(book: CompleteBook?): List<BookLink> {
    if (book == null) {
      return emptyList()
    }

    return linkRepository.generateBookLinks(book)
  }

  fun toggleFavorite() = coroutineScope.launch {
    booksRepository.toggleFavorite(bookId)
  }

  fun delete() = coroutineScope.launch {
    booksRepository.delete(bookId)
  }

  fun findSeriesVolumes(book: CompleteBook?): Flow<BookNeighbors?> {
    if (book == null) {
      return flowOf(null)
    }

    return booksRepository.findSeriesVolumes(
      title = book.title.toTitleParts(),
      publisherId = book.publisher_id,
      groupId = book.group_id
    )
  }

  fun shareImage(bitmap: Bitmap?, book: CompleteBook) = coroutineScope.launch {
    if (bitmap == null) {
      return@launch
    }

    val message = buildString {
      appendLine(book.title)
      append(book.publisher_name)
      append(" · ")
      append(book.label_price_value.toLocaleCurrencyString(book.label_price_currency))
    }

    val image = ImageSaver.Image(
      bitmap = bitmap,
      fileName = "${context.appName.lowercase()}-book-${book.id}",
      location = ImageSaver.Location.Cache
    )

    when (val imageResult = imageSaver.saveImage(image)) {
      is ImageSaver.Result.Success -> {
        imageResult.uri
          ?.toShareIntent(context, message = message)
          ?.let { context.startActivity(it) }
      }
      is ImageSaver.Result.Failure -> {
        context.toast(R.string.error_during_image_save)
      }
    }
  }

  fun saveImage(bitmap: Bitmap?, book: CompleteBook) = coroutineScope.launch {
    if (bitmap == null) {
      return@launch
    }

    val image = ImageSaver.Image(
      bitmap = bitmap,
      fileName = "${context.appName.lowercase()}-book-${book.id}",
      location = ImageSaver.Location.Downloads
    )

    when (imageSaver.saveImage(image)) {
      is ImageSaver.Result.Success -> context.toast(R.string.image_saved_successfully)
      is ImageSaver.Result.Failure -> context.toast(R.string.error_during_image_save)
    }
  }

  fun openLink(link: BookLink) {
    val intent = Intent(Intent.ACTION_VIEW, link.url.toUri())
    context.startActivity(intent)
  }

}