package io.github.alessandrojean.toshokan.presentation.ui.book

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.palette.graphics.Palette
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

  var palette by mutableStateOf<Palette?>(null)
    private set

  fun toggleFavorite() = coroutineScope.launch {
    booksRepository.toggleFavorite(bookId)
  }

  fun delete() = coroutineScope.launch {
    booksRepository.delete(bookId)
  }

  fun findPalette(image: Bitmap?) {
    image?.let {
      palette = Palette.Builder(it).generate()
    }
  }

}