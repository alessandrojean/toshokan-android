package io.github.alessandrojean.toshokan.presentation.ui.book

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.database.data.Book
import io.github.alessandrojean.toshokan.repository.BooksRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
  private val booksRepository: BooksRepository
) : ViewModel() {

  var bookId by mutableStateOf<Long?>(null)
    private set

  fun findTheBook(bookId: Long): Flow<Book> {
    this.bookId = bookId

    return booksRepository.findById(bookId)
  }

}