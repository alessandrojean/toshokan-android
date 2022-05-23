package io.github.alessandrojean.toshokan.presentation.ui.publishers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.database.data.Publisher
import io.github.alessandrojean.toshokan.presentation.ui.publishers.manage.ManagePublisherMode
import io.github.alessandrojean.toshokan.repository.PublishersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PublishersState(
  val publishers: Flow<List<Publisher>>,
  val showManageDialog: Boolean = false,
  val showDeleteWarning: Boolean = false,
  val selected: List<Long> = emptyList(),
  val manageDialogMode: ManagePublisherMode = ManagePublisherMode.CREATE
)

@HiltViewModel
class PublishersViewModel @Inject constructor(
  private val publishersRepository: PublishersRepository
) : ViewModel() {
  private val _uiState = MutableStateFlow(
    PublishersState(
      publishers = publishersRepository.publishers
    )
  )
  val uiState: StateFlow<PublishersState> = _uiState.asStateFlow()

  fun changeManageDialogMode(newMode: ManagePublisherMode) = viewModelScope.launch {
    _uiState.update { it.copy(manageDialogMode = newMode) }
  }

  fun showManageDialog() = viewModelScope.launch {
    _uiState.update { it.copy(showManageDialog = true) }
  }

  fun hideManageDialog() = viewModelScope.launch {
    _uiState.update {
      it.copy(
        showManageDialog = false,
        selected = emptyList(),
        manageDialogMode = ManagePublisherMode.CREATE
      )
    }
  }

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