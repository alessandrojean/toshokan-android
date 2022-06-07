package io.github.alessandrojean.toshokan.presentation.ui.book

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import cafe.adriel.voyager.hilt.ScreenModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.alessandrojean.toshokan.repository.BooksRepository
import kotlinx.coroutines.launch

class BookScreenModel @AssistedInject constructor(
  private val booksRepository: BooksRepository,
  @Assisted private val bookId: Long
) : ScreenModel {

  @AssistedFactory
  interface Factory : ScreenModelFactory {
    fun create(@Assisted bookId: Long): BookScreenModel
  }

  val book = booksRepository.findById(bookId)
  val contributors = booksRepository.findBookContributorsFlow(bookId)

  fun toggleFavorite() = coroutineScope.launch {
    booksRepository.toggleFavorite(bookId)
  }

  fun delete() = coroutineScope.launch {
    booksRepository.delete(bookId)
  }

}