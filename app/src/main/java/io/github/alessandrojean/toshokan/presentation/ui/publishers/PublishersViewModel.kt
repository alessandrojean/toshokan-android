package io.github.alessandrojean.toshokan.presentation.ui.publishers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.repository.PublishersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PublishersState(
  val showDeleteWarning: Boolean = false,
  val selected: List<Long> = emptyList(),
)

@HiltViewModel
class PublishersViewModel @Inject constructor(
  private val publishersRepository: PublishersRepository
) : ViewModel() {
  private val _uiState = MutableStateFlow(PublishersState())
  val uiState: StateFlow<PublishersState> = _uiState.asStateFlow()

  val publishers = publishersRepository.publishers

  fun showDeleteWarning() = viewModelScope.launch {
    _uiState.update { it.copy(showDeleteWarning = true) }
  }

  fun hideDeleteWarning() = viewModelScope.launch {
    _uiState.update { it.copy(showDeleteWarning = false) }
  }

  fun clearSelection() = viewModelScope.launch {
    _uiState.update { it.copy(selected = emptyList()) }
  }

  fun toggleSelection(id: Long) = viewModelScope.launch {
    if (id !in uiState.value.selected) {
      _uiState.update { it.copy(selected = uiState.value.selected + id) }
    } else {
      _uiState.update {
        it.copy(
          selected = uiState.value.selected.filter { selId -> selId != id }
        )
      }
    }
  }

  fun deleteSelected() = viewModelScope.launch {
    publishersRepository.bulkDelete(uiState.value.selected)
    clearSelection()
  }
}