package io.github.alessandrojean.toshokan.presentation.ui.groups

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.database.data.BookGroup
import io.github.alessandrojean.toshokan.presentation.ui.groups.manage.ManageGroupMode
import io.github.alessandrojean.toshokan.repository.GroupsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupsState(
  val groups: Flow<List<BookGroup>>,
  val showManageDialog: Boolean = false,
  val showDeleteWarning: Boolean = false,
  val selected: List<Long> = emptyList(),
  val manageDialogMode: ManageGroupMode = ManageGroupMode.CREATE,
  val reorderMode: Boolean = false,
)

@HiltViewModel
class GroupsViewModel @Inject constructor(
  private val groupsRepository: GroupsRepository
) : ViewModel() {
  private val _uiState = MutableStateFlow(
    GroupsState(
      groups = groupsRepository.groups
    )
  )
  val uiState: StateFlow<GroupsState> = _uiState.asStateFlow()

  fun changeManageDialogMode(newMode: ManageGroupMode) = viewModelScope.launch {
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
        manageDialogMode = ManageGroupMode.CREATE
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
    groupsRepository.bulkDelete(uiState.value.selected)
    clearSelection()
  }

  fun enterReorderMode() = viewModelScope.launch {
    _uiState.update { it.copy(reorderMode = true) }
  }

  fun exitReorderMode() = viewModelScope.launch {
    _uiState.update { it.copy(reorderMode = false) }
  }

  fun reorderItems(reorderedItemsIds: List<Long>) = viewModelScope.launch {
    reorderedItemsIds.forEachIndexed { sort, id ->
      groupsRepository.updateSort(id, sort.toLong())
    }
  }
}