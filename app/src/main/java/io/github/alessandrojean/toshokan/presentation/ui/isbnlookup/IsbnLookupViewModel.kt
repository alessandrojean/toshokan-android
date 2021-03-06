package io.github.alessandrojean.toshokan.presentation.ui.isbnlookup

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.data.preference.PreferencesManager
import io.github.alessandrojean.toshokan.repository.BooksRepository
import io.github.alessandrojean.toshokan.service.lookup.LookupBookResult
import io.github.alessandrojean.toshokan.service.lookup.LookupRepository
import io.github.alessandrojean.toshokan.service.lookup.LookupResult
import io.github.alessandrojean.toshokan.util.ConnectionState
import io.github.alessandrojean.toshokan.util.isValidIsbn
import io.github.alessandrojean.toshokan.util.observeConnectivityAsFlow
import io.github.alessandrojean.toshokan.util.removeDashes
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class IsbnLookupState {
  EMPTY,
  NO_INTERNET,
  LOADING,
  RESULTS,
  NO_RESULTS,
  ERROR,
  HISTORY
}

@HiltViewModel
class IsbnLookupViewModel @Inject constructor(
  application: Application,
  private val lookupRepository: LookupRepository,
  private val preferencesManager: PreferencesManager
) : AndroidViewModel(application) {

  var searchQuery by mutableStateOf(TextFieldValue(""))
  var results = mutableStateListOf<LookupBookResult>()
  var progress by mutableStateOf(0f)
  var error by mutableStateOf<Throwable?>(null)
  var state by mutableStateOf(IsbnLookupState.EMPTY)

  private val history = preferencesManager.isbnLookupSearchHistory()
  val historyFlow = history.asObjectFlow()

  private val context
    get() = getApplication<Application>()

  init {
    setupConnectivityObserver()
  }

  private var searchJob: Job? = null

  private fun hasSearchHistory(): Boolean {
    return preferencesManager.isbnLookupSearchHistory().get().isNotEmpty()
  }

  private fun setupConnectivityObserver() = viewModelScope.launch {
    context.observeConnectivityAsFlow()
      .cancellable()
      .collect {
        state = if (it is ConnectionState.Available) {
          if (hasSearchHistory()) IsbnLookupState.HISTORY else IsbnLookupState.EMPTY
        } else {
          cancelSearch()
          IsbnLookupState.NO_INTERNET
        }
      }
  }

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
      lookupRepository.searchByIsbn(searchQuery.text)
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
    if (!searchQuery.text.isValidIsbn()) {
      return
    }

    val query = searchQuery.text.removeDashes()
    val oldHistory = history.getObject().filter { it != query }
    val newHistory = (listOf(query) + oldHistory).take(20)

    viewModelScope.launch {
      history.editObject(newHistory)
    }
  }

  fun removeHistoryItem(item: String) {
    val newHistory = preferencesManager.isbnLookupSearchHistory().getObject().filter { it != item }

    viewModelScope.launch {
      history.editObject(newHistory)

      if (newHistory.isEmpty()) {
        state = IsbnLookupState.EMPTY
      }
    }
  }
}
