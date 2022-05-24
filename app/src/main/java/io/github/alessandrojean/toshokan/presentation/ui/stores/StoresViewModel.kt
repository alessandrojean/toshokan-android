package io.github.alessandrojean.toshokan.presentation.ui.stores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.database.data.Store
import io.github.alessandrojean.toshokan.presentation.ui.stores.manage.ManageStoreMode
import io.github.alessandrojean.toshokan.repository.StoresRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StoresState(
  val stores: Flow<List<Store>>,
  val showManageDialog: Boolean = false,
  val showDeleteWarning: Boolean = false,
  val selected: List<Long> = emptyList(),
  val manageDialogMode: ManageStoreMode = ManageStoreMode.CREATE
)

@HiltViewModel
class StoresViewModel @Inject constructor(
  private val storesRepository: StoresRepository
) : ViewModel() {
  private val _uiState = MutableStateFlow(
    StoresState(
      stores = storesRepository.stores
    )
  )
  val uiState: StateFlow<StoresState> = _uiState.asStateFlow()

  fun changeManageDialogMode(newMode: ManageStoreMode) = viewModelScope.launch {
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
        manageDialogMode = ManageStoreMode.CREATE
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
    storesRepository.bulkDelete(uiState.value.selected)
    clearSelection()
  }
}