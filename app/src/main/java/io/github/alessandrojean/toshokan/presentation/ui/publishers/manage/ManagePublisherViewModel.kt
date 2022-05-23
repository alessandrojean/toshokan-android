package io.github.alessandrojean.toshokan.presentation.ui.publishers.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.database.data.Publisher
import io.github.alessandrojean.toshokan.repository.PublishersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManagePublisherState(
  val id: Long? = null,
  val name: String = "",
  val description: String = "",
  val website: String = "",
  val instagramProfile: String = "",
  val twitterProfile: String = "",
  val writing: Boolean = false
)

enum class ManagePublisherMode {
  CREATE,
  EDIT
}

@HiltViewModel
class ManagePublisherViewModel @Inject constructor(
  private val publishersRepository: PublishersRepository
) : ViewModel() {
  private val _uiState = MutableStateFlow(ManagePublisherState())
  val uiState: StateFlow<ManagePublisherState> = _uiState.asStateFlow()

  fun onNameChanged(newName: String) = viewModelScope.launch {
    _uiState.update { it.copy(name = newName) }
  }

  fun onDescriptionChanged(newDescription: String) = viewModelScope.launch {
    _uiState.update { it.copy(description = newDescription) }
  }

  fun onWebsiteChanged(newWebsite: String) = viewModelScope.launch {
    _uiState.update { it.copy(website = newWebsite) }
  }

  fun onInstagramProfileChanged(newInstagramProfile: String) = viewModelScope.launch {
    _uiState.update { it.copy(instagramProfile = newInstagramProfile) }
  }

  fun onTwitterProfileChanged(newTwitterProfile: String) = viewModelScope.launch {
    _uiState.update { it.copy(instagramProfile = newTwitterProfile) }
  }

  private fun clearFields() = viewModelScope.launch {
    _uiState.update {
      it.copy(
        id = null,
        name = "",
        description = "",
        website = "",
        instagramProfile = "",
        twitterProfile = ""
      )
    }
  }

  fun setFieldValues(publisher: Publisher) = viewModelScope.launch {
    _uiState.update {
      it.copy(
        id = publisher.id,
        name = publisher.name,
        description = publisher.description.orEmpty(),
        website = publisher.website.orEmpty(),
        instagramProfile = publisher.instagram_user.orEmpty(),
        twitterProfile = publisher.twitter_user.orEmpty()
      )
    }
  }

  private fun validate(): Boolean = uiState.value.name.isNotEmpty()

  fun create() = viewModelScope.launch {
    if (!validate()) {
      return@launch
    }

    _uiState.update { it.copy(writing = true) }

    publishersRepository.insert(
      name = uiState.value.name,
      description = uiState.value.description,
      website = uiState.value.website,
      instagramProfile = uiState.value.instagramProfile,
      twitterProfile = uiState.value.twitterProfile
    )

    clearFields()
    _uiState.update { it.copy(writing = false) }
  }

  fun edit() = viewModelScope.launch {
    if (!validate()) {
      return@launch
    }

    _uiState.update { it.copy(writing = true) }

    publishersRepository.update(
      id = uiState.value.id!!,
      name = uiState.value.name,
      description = uiState.value.description,
      website = uiState.value.website,
      instagramProfile = uiState.value.instagramProfile,
      twitterProfile = uiState.value.twitterProfile,
    )

    clearFields()
    _uiState.update { it.copy(writing = false) }
  }
}