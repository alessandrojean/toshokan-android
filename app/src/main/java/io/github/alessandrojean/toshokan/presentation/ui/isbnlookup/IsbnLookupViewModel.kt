package io.github.alessandrojean.toshokan.presentation.ui.isbnlookup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.data.preference.PreferencesManager
import io.github.alessandrojean.toshokan.repository.BooksRepository
import io.github.alessandrojean.toshokan.service.lookup.LookupBookResult
import io.github.alessandrojean.toshokan.service.lookup.LookupRepository
import io.github.alessandrojean.toshokan.service.lookup.LookupResult
import io.github.alessandrojean.toshokan.util.isValidIsbn
import io.github.alessandrojean.toshokan.util.removeDashes
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class IsbnLookupState {
  EMPTY,
  LOADING,
  RESULTS,
  NO_RESULTS,
  ERROR,
  HISTORY
}

@HiltViewModel
class IsbnLookupViewModel @Inject constructor(
  private val booksRepository: BooksRepository,
  private val lookupRepository: LookupRepository,
  private val preferencesManager: PreferencesManager
) : ViewModel() {

  var searchQuery by mutableStateOf("")
  var results = mutableStateListOf<LookupBookResult>()
  var progress by mutableStateOf(0f)
  var error by mutableStateOf<Throwable?>(null)
  var state by mutableStateOf(IsbnLookupState.EMPTY)
  val history = preferencesManager.isbnLookupSearchHistory().asFlow()

  init {
    if (preferencesManager.isbnLookupSearchHistory().get().isNotEmpty()) {
      state = IsbnLookupState.HISTORY
    }
  }

  private var searchJob: Job? = null

  fun cancelSearch() {
    if (searchJob?.isActive == true) {
      searchJob?.cancel()
      progress = 1f
    }
  }

  fun search() {
    cancelSearch()

    state = IsbnLookupState.LOADING
    updateHistory()

    searchJob = viewModelScope.launch {
      lookupRepository.searchByIsbn(searchQuery)
        .collect { searchState ->
          when (searchState) {
            is LookupResult.Loading -> {
              state = IsbnLookupState.LOADING
              error = null
              progress = 0f
              results.clear()
            }
            is LookupResult.Result -> {
              state = if (searchState.progress == 1f && searchState.results.isEmpty()) {
                IsbnLookupState.NO_RESULTS
              } else {
                IsbnLookupState.RESULTS
              }

              progress = searchState.progress
              results.clear()
              results.addAll(searchState.results)
            }
            is LookupResult.Error -> {
              if (searchState.progress == 1f && searchState.lastResults.isEmpty()) {
                state = IsbnLookupState.ERROR
                error = searchState.error
              } else {
                state = IsbnLookupState.RESULTS
              }

              progress = searchState.progress
              results.clear()
              results.addAll(searchState.lastResults)
            }
          }
        }
    }
  }

  private fun updateHistory() {
    if (!searchQuery.isValidIsbn()) {
      return
    }

    val oldHistory = preferencesManager.isbnLookupSearchHistory().get().toList()
    val newHistory = (listOf(searchQuery) + oldHistory)
      .take(20)
      .distinct()
      .toSet()

    viewModelScope.launch {
      preferencesManager.isbnLookupSearchHistory().setAndCommit(newHistory)
    }
  }

  fun removeHistoryItem(item: String) {
    val newHistory = preferencesManager.isbnLookupSearchHistory().get()
      .filter { it != item }

    viewModelScope.launch {
      preferencesManager.isbnLookupSearchHistory()
        .setAndCommit(newHistory.toSet())

      if (newHistory.isEmpty()) {
        state = IsbnLookupState.EMPTY
      }
    }
  }

  fun checkDuplicates(): Long? {
    return booksRepository.findByCode(searchQuery.removeDashes())?.id
  }
}
