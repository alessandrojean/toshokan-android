package io.github.alessandrojean.toshokan.presentation.ui.isbnlookup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.service.lookup.LookupRepository
import io.github.alessandrojean.toshokan.service.lookup.LookupBookResult
import io.github.alessandrojean.toshokan.service.lookup.LookupResult
import io.github.alessandrojean.toshokan.service.lookup.Provider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import logcat.logcat
import javax.inject.Inject

data class IsbnLookupState(
  val error: Throwable? = null,
  val loading: Boolean = false,
  val progress: Float = 0f,
  val searchQuery: String = "",
  val searchedOnce: Boolean = false
)

@HiltViewModel
class IsbnLookupViewModel @Inject constructor(
  private val lookupRepository: LookupRepository
) : ViewModel() {
  private val _uiState = MutableStateFlow(IsbnLookupState())
  val uiState: StateFlow<IsbnLookupState> = _uiState.asStateFlow()

  val results = mutableStateListOf<LookupBookResult>()

  var searchJob: Job? = null
    private set

  fun onSearchQueryChange(newSearchQuery: String) {
    _uiState.update { it.copy(searchQuery = newSearchQuery) }
  }

  fun search() {
    if (searchJob?.isActive == true) {
      searchJob?.cancel()
    }

    _uiState.update { it.copy(loading = true, searchedOnce = true) }

    searchJob = viewModelScope.launch {
      lookupRepository.searchByIsbn(uiState.value.searchQuery)
        .collect { state ->
          when (state) {
            is LookupResult.Loading -> {
              _uiState.update {
                it.copy(
                  loading = true,
                  progress = 0f,
                  error = null,
                )
              }
              results.clear()
            }
            is LookupResult.Result -> {
              _uiState.update {
                it.copy(
                  loading = state.progress < 1f,
                  progress = state.progress,
                )
              }
              results.clear()
              results.addAll(state.results.toList())
            }
            is LookupResult.Error -> {
              _uiState.update {
                it.copy(
                  loading = state.progress < 1f,
                  progress = state.progress,
                  error = state.error,
                )
              }
              results.clear()
              results.addAll(state.lastResults.toList())
            }
          }
        }
    }
  }
}
