package io.github.alessandrojean.toshokan.presentation.ui.book

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.net.toUri
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import cafe.adriel.voyager.hilt.ScreenModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ActivityContext
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.data.preference.PreferencesManager
import io.github.alessandrojean.toshokan.data.storage.ImageSaver
import io.github.alessandrojean.toshokan.database.data.Book
import io.github.alessandrojean.toshokan.database.data.BookContributor
import io.github.alessandrojean.toshokan.database.data.CompleteBook
import io.github.alessandrojean.toshokan.domain.BookNeighbors
import io.github.alessandrojean.toshokan.repository.BooksRepository
import io.github.alessandrojean.toshokan.service.link.BookLink
import io.github.alessandrojean.toshokan.service.link.LinkCategory
import io.github.alessandrojean.toshokan.service.link.LinkRepository
import io.github.alessandrojean.toshokan.util.extension.toLocaleCurrencyString
import io.github.alessandrojean.toshokan.util.extension.toShareIntent
import io.github.alessandrojean.toshokan.util.extension.toTitleParts
import io.github.alessandrojean.toshokan.util.extension.toast
import io.github.alessandrojean.toshokan.util.storage.DiskUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class BookScreenModel @AssistedInject constructor(
  @ActivityContext private val context: Context,
  private val booksRepository: BooksRepository,
  preferencesManager: PreferencesManager,
  private val imageSaver: ImageSaver,
  private val linkRepository: LinkRepository,
  @Assisted private var bookId: Long
) : StateScreenModel<BookScreenModel.State>(State.Loading) {

  @AssistedFactory
  interface Factory : ScreenModelFactory {
    fun create(@Assisted bookId: Long): BookScreenModel
  }

  sealed class State {
    object Loading : State()
    object NotFound : State()
    data class Result(
      val book: CompleteBook?,
      val simpleBook: Book?,
      val contributors: List<BookContributor> = emptyList(),
      val neighbors: BookNeighbors?,
      val links: Map<LinkCategory, List<BookLink>> = emptyMap()
    ) : State()
  }

  val showBookNavigation = preferencesManager.showBookNavigation().asFlow()
  private var observeJob: Job? = null

  init {
    observeBook()
  }

  fun navigate(otherBookId: Long) {
    bookId = otherBookId
    observeBook()
  }

  private fun observeBook() {
    observeJob?.cancel()

    observeJob = coroutineScope.launch {
      val book = booksRepository.findById(bookId)

      if (book == null) {
        mutableState.value = State.NotFound
        return@launch
      }

      val combinedFlows = combine(
        booksRepository.findByIdAsFlow(bookId),
        booksRepository.findSimpleById(bookId),
        booksRepository.findBookContributorsAsFlow(bookId),
        booksRepository.findSeriesVolumes(book)
      ) { bookDb, simpleBook, contributors, neighbors ->
        if (bookDb != null && simpleBook != null) {
          State.Result(
            book = bookDb,
            simpleBook = simpleBook,
            contributors = contributors,
            neighbors = neighbors,
            links = findBookLinks(bookDb)
          )
        } else {
          State.Loading
        }
      }

      combinedFlows.collect { mutableState.value = it }
    }
  }

  private fun findBookLinks(book: CompleteBook?): Map<LinkCategory, List<BookLink>> {
    if (book == null) {
      return emptyMap()
    }

    return linkRepository
      .generateBookLinks(book)
      .sortedBy { context.getString(it.name) }
      .groupBy { it.category }
  }

  fun toggleFavorite() = coroutineScope.launch {
    booksRepository.toggleFavorite(bookId)
  }

  fun delete(block: () -> Unit = {}) = coroutineScope.launch {
    booksRepository.delete(bookId)
    block()
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
      fileName = DiskUtil.hashKeyForDisk("${book.group_name}-${book.title}-${book.publisher_name}"),
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
      fileName = DiskUtil.hashKeyForDisk("${book.group_name}-${book.title}-${book.publisher_name}"),
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

  fun deleteCover(coverUrl: String?) = coroutineScope.launch {
    booksRepository.deleteCover(bookId, coverUrl)
  }

}