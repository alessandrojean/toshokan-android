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
import io.github.alessandrojean.toshokan.domain.BookNeighbors
import io.github.alessandrojean.toshokan.domain.DomainBook
import io.github.alessandrojean.toshokan.domain.DomainContributor
import io.github.alessandrojean.toshokan.domain.DomainTag
import io.github.alessandrojean.toshokan.presentation.ui.book.BookScreen.BookData
import io.github.alessandrojean.toshokan.repository.BooksRepository
import io.github.alessandrojean.toshokan.service.link.BookLink
import io.github.alessandrojean.toshokan.service.link.LinkCategory
import io.github.alessandrojean.toshokan.service.link.LinkRepository
import io.github.alessandrojean.toshokan.util.extension.SheetUtils
import io.github.alessandrojean.toshokan.util.extension.toLocaleCurrencyString
import io.github.alessandrojean.toshokan.util.extension.toShareImageIntent
import io.github.alessandrojean.toshokan.util.extension.toShareIntent
import io.github.alessandrojean.toshokan.util.extension.toast
import io.github.alessandrojean.toshokan.util.storage.DiskUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class BookScreenModel @AssistedInject constructor(
  @ActivityContext private val context: Context,
  private val booksRepository: BooksRepository,
  preferencesManager: PreferencesManager,
  private val imageSaver: ImageSaver,
  private val linkRepository: LinkRepository,
  @Assisted private var bookData: BookData
) : StateScreenModel<BookScreenModel.State>(State.Loading) {

  @AssistedFactory
  interface Factory : ScreenModelFactory {
    fun create(@Assisted bookData: BookData): BookScreenModel
  }

  sealed class State {
    object Loading : State()
    object Writing : State()
    object NotFound : State()
    data class Result(
      val book: DomainBook?,
      val contributors: List<DomainContributor> = emptyList(),
      val tags: List<DomainTag> = emptyList(),
      val neighbors: BookNeighbors? = null,
      val links: Map<LinkCategory, List<BookLink>> = emptyMap()
    ) : State()
  }

  val showBookNavigation = preferencesManager.showBookNavigation().asFlow()
  private var observeJob: Job? = null

  val inLibrary: Boolean
    get () = bookData is BookData.Database

  init {
    when (bookData) {
      is BookData.Database -> {
        observeBook()
      }
      is BookData.External -> {
        copyDataExternal()
      }
    }
  }

  fun navigate(otherBookId: Long) {
    bookData = BookData.Database(otherBookId)
    observeBook()
  }

  private fun copyDataExternal() = coroutineScope.launch {
    val externalData = (bookData as BookData.External).book

    mutableState.value = State.Result(
      book = externalData,
      contributors = externalData.contributors,
      tags = externalData.tags,
      links = findBookLinks(externalData)
    )
  }

  private fun observeBook() {
    observeJob?.cancel()

    if (bookData !is BookData.Database) {
      return
    }

    observeJob = coroutineScope.launch {
      val bookId = (bookData as BookData.Database).bookId
      val book = booksRepository.findById(bookId)

      if (book == null) {
        mutableState.value = State.NotFound
        return@launch
      }

      val combinedFlows = combine(
        booksRepository.findByIdAsFlow(bookId),
        booksRepository.findBookContributorsAsFlow(bookId),
        booksRepository.findBookTagsAsFlow(bookId),
        booksRepository.findSeriesVolumes(book)
      ) { bookDb, contributors, tags, neighbors ->
        if (bookDb != null) {
          State.Result(
            book = bookDb,
            contributors = contributors,
            tags = tags,
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

  private fun findBookLinks(book: DomainBook?): Map<LinkCategory, List<BookLink>> {
    if (book == null) {
      return emptyMap()
    }

    return linkRepository
      .generateBookLinks(book)
      .sortedBy { context.getString(it.name) }
      .groupBy { it.category }
  }

  fun toggleFavorite() = coroutineScope.launch {
    if (state.value is State.Result) {
      booksRepository.toggleFavorite((state.value as State.Result).book!!.id!!)
    }
  }

  fun delete(block: () -> Unit = {}) = coroutineScope.launch {
    if (state.value is State.Result) {
      booksRepository.delete((state.value as State.Result).book!!.id!!)
      block()
    }
  }

  fun shareImage(bitmap: Bitmap?, book: DomainBook) = coroutineScope.launch {
    if (bitmap == null) {
      return@launch
    }

    val message = buildString {
      appendLine(book.title)
      append(book.publisher.title!!)
      append(" · ")
      append(book.labelPrice.value.toLocaleCurrencyString(book.labelPrice.currency))
    }

    val image = ImageSaver.Image(
      bitmap = bitmap,
      fileName = DiskUtil.hashKeyForDisk("${book.group.title!!}-${book.title}-${book.publisher.title!!}"),
      location = ImageSaver.Location.Cache
    )

    when (val imageResult = imageSaver.saveImage(image)) {
      is ImageSaver.Result.Success -> {
        imageResult.uri
          ?.toShareImageIntent(context, message = message)
          ?.let { context.startActivity(it) }
      }
      is ImageSaver.Result.Failure -> {
        context.toast(R.string.error_during_image_save)
      }
    }
  }

  fun saveImage(bitmap: Bitmap?, book: DomainBook) = coroutineScope.launch {
    if (bitmap == null) {
      return@launch
    }

    val image = ImageSaver.Image(
      bitmap = bitmap,
      fileName = DiskUtil.hashKeyForDisk("${book.group.title!!}-${book.title}-${book.publisher.title!!}"),
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
    if (state.value is State.Result) {
      booksRepository.deleteCover((state.value as State.Result).book!!.id!!, coverUrl)
    }
  }

  fun addToLibrary(onFinish: () -> Unit = {}) {
    (bookData as? BookData.External)?.book?.let { book ->
      mutableState.value = State.Writing

      coroutineScope.launch {
        booksRepository.insertDomain(book)?.let { bookId ->
          bookData = BookData.Database(bookId)
          mutableState.value = State.Loading
          observeBook()
          onFinish()
        }
      }
    }
  }

  fun shareWebUrl() {
    (state.value as? State.Result)?.let { resultState ->
      if (resultState.book != null) {
        val book = resultState.book.copy(
          contributors = resultState.contributors,
          tags = resultState.tags
        )

        SheetUtils.createBookShareUrl(book)
          .toShareIntent(context)
          .let { context.startActivity(it) }
      }
    }
  }

}