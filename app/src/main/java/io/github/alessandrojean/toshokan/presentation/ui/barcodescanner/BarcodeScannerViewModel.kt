package io.github.alessandrojean.toshokan.presentation.ui.barcodescanner

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.repository.BooksRepository
import io.github.alessandrojean.toshokan.util.removeDashes
import javax.inject.Inject

@HiltViewModel
class BarcodeScannerViewModel @Inject constructor(
  private val booksRepository: BooksRepository
) : ViewModel() {

  fun checkDuplicates(code: String): Long? {
    return booksRepository.findByCode(code.removeDashes())?.id
  }

}