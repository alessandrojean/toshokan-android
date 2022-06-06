package io.github.alessandrojean.toshokan.presentation.ui.book.reading

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import cafe.adriel.voyager.hilt.ScreenModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.alessandrojean.toshokan.repository.BooksRepository
import kotlinx.coroutines.launch

class ReadingScreenModel @AssistedInject constructor(
  private val booksRepository: BooksRepository,
  @Assisted private val bookId: Long
) : ScreenModel {

  @AssistedFactory
  interface Factory : ScreenModelFactory {
    fun create(@Assisted bookId: Long): ReadingScreenModel
  }

  val readings = booksRepository.findReadings(bookId)

  val selection = mutableStateListOf<Long>()

  val selectionMode by derivedStateOf {
    selection.isNotEmpty()
  }

  fun createReading(readAt: Long?) = coroutineScope.launch {
    booksRepository.insertReading(bookId, readAt)
  }

  fun clearSelection() {
    selection.clear()
  }

  fun toggleSelection(id: Long) {
    if (id in selection) {
      selection.remove(id)
    } else {
      selection.add(id)
    }
  }

  fun deleteSelection() = coroutineScope.launch {
    booksRepository.bulkDeleteReadings(selection)
    selection.clear()
  }

}