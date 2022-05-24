package io.github.alessandrojean.toshokan.presentation.ui.groups.manage

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.database.data.BookGroup
import io.github.alessandrojean.toshokan.repository.GroupsRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ManageGroupMode {
  CREATE,
  EDIT
}

@HiltViewModel
class ManageGroupViewModel @Inject constructor(
  private val groupsRepository: GroupsRepository
) : ViewModel() {

  var id by mutableStateOf<Long?>(null)
  var name by mutableStateOf("")
  var writing by mutableStateOf(false)

  private val formInvalid by derivedStateOf { name.isEmpty() }

  fun clearFields() = viewModelScope.launch {
    id = null
    name = ""
  }

  fun setFieldValues(group: BookGroup) = viewModelScope.launch {
    id = group.id
    name = group.name
  }

  fun create() = viewModelScope.launch {
    if (formInvalid) {
      return@launch
    }

    writing = true

    groupsRepository.insert(name.trim())

    clearFields()
    writing = false
  }

  fun edit() = viewModelScope.launch {
    if (formInvalid) {
      return@launch
    }

    writing = true

    groupsRepository.update(id!!, name.trim())

    clearFields()
    writing = false
  }
}