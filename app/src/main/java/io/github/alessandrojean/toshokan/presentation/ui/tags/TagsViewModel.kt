package io.github.alessandrojean.toshokan.presentation.ui.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.presentation.ui.groups.manage.ManageGroupMode
import io.github.alessandrojean.toshokan.presentation.ui.tags.manage.ManageTagMode
import io.github.alessandrojean.toshokan.repository.GroupsRepository
import io.github.alessandrojean.toshokan.repository.TagsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TagsState(
  val showManageDialog: Boolean = false,
  val showDeleteWarning: Boolean = false,
  val selected: List<Long> = emptyList(),
  val manageDialogMode: ManageTagMode = ManageTagMode.CREATE,
)

@HiltViewModel
class TagsViewModel @Inject constructor(
  private val tagsRepository: TagsRepository
) : ViewModel() {
  private val _uiState = MutableStateFlow(TagsState())
  val uiState: StateFlow<TagsState> = _uiState.asStateFlow()

  val tags = tagsRepository.subscribeToTags()

  fun changeManageDialogMode(newMode: ManageTagMode) = viewModelScope.launch {
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
        manageDialogMode = ManageTagMode.CREATE
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
    tagsRepository.bulkDelete(uiState.value.selected)
    clearSelection()
  }

}