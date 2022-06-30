package io.github.alessandrojean.toshokan.presentation.ui.tags.manage

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.database.data.Tag
import io.github.alessandrojean.toshokan.repository.TagsRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ManageTagMode {
  CREATE,
  EDIT
}

@HiltViewModel
class ManageTagViewModel @Inject constructor(
  private val tagsRepository: TagsRepository
) : ViewModel() {

  var id by mutableStateOf<Long?>(null)
  var name by mutableStateOf("")
  var isNsfw by mutableStateOf(false)
  var writing by mutableStateOf(false)

  private val formInvalid by derivedStateOf { name.isEmpty() }

  fun clearFields() = viewModelScope.launch {
    id = null
    name = ""
    isNsfw = false
  }

  fun setFieldValues(tag: Tag) = viewModelScope.launch {
    id = tag.id
    name = tag.name
    isNsfw = tag.is_nsfw
  }

  fun create() = viewModelScope.launch {
    if (formInvalid) {
      return@launch
    }

    writing = true

    tagsRepository.insert(name.trim(), isNsfw)

    clearFields()
    writing = false
  }

  fun edit() = viewModelScope.launch {
    if (formInvalid) {
      return@launch
    }

    writing = true

    tagsRepository.update(id!!, name.trim(), isNsfw)

    clearFields()
    writing = false
  }
}