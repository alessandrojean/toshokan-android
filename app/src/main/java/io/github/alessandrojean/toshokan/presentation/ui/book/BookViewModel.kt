package io.github.alessandrojean.toshokan.presentation.ui.book

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.database.data.BookContributor
import io.github.alessandrojean.toshokan.database.data.CompleteBook
import io.github.alessandrojean.toshokan.repository.BooksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
  private val booksRepository: BooksRepository
) : ViewModel() {

  var bookId by mutableStateOf<Long?>(null)
    private set

  fun findTheBook(bookId: Long): Flow<CompleteBook?> {
    this.bookId = bookId

    return booksRepository.findById(bookId)
  }

  fun findTheBookContributors(bookId: Long): Flow<List<BookContributor>> {
    return booksRepository.findBookContributorsFlow(bookId)
  }

  fun delete() = viewModelScope.launch {
    booksRepository.delete(bookId!!)
  }

}