package io.github.alessandrojean.toshokan.presentation.ui.library

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.repository.BooksRepository
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
  private val booksRepository: BooksRepository
) : ViewModel() {

  val library = booksRepository.findLibraryBooks(isFuture = false)

}