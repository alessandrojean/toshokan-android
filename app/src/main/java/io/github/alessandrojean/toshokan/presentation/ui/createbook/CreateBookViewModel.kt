package io.github.alessandrojean.toshokan.presentation.ui.createbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.database.ToshokanDatabase
import io.github.alessandrojean.toshokan.service.lookup.LookupRepository
import io.github.alessandrojean.toshokan.service.lookup.LookupBookResult
import io.github.alessandrojean.toshokan.service.lookup.Provider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class CreateBookState(
  val loading: Boolean = false,
  val results: Map<Provider, List<LookupBookResult>> = emptyMap(),
  val selected: LookupBookResult? = null,
  val searchQuery: String = "",
  val searchedOnce: Boolean = false,
  val creating: Boolean = false,
)

@HiltViewModel
class CreateBookViewModel @Inject constructor(
  private val lookupRepository: LookupRepository
) : ViewModel() {
  private val _uiState = MutableStateFlow(CreateBookState())
  val uiState: StateFlow<CreateBookState> = _uiState.asStateFlow()

  fun onSearchQueryChange(newSearchQuery: String) {
    _uiState.update { it.copy(searchQuery = newSearchQuery) }
  }

  fun onSelectedChange(newSelected: LookupBookResult) {
    _uiState.update { it.copy(selected = newSelected) }
  }

  fun search() {
    _uiState.update { it.copy(loading = true, searchedOnce = true) }

    viewModelScope.launch {
      val results = lookupRepository.searchByIsbn(_uiState.value.searchQuery)

      _uiState.update {
        it.copy(loading = false, results = results)
      }
    }
  }

  fun create() {
    _uiState.update { it.copy(creating = true) }

    viewModelScope.launch {

    }
  }
}