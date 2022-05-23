package io.github.alessandrojean.toshokan.presentation.ui.people.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.database.data.Person
import io.github.alessandrojean.toshokan.repository.PeopleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManagePeopleState(
  val id: Long? = null,
  val name: String = "",
  val description: String = "",
  val country: String = "",
  val website: String = "",
  val instagramProfile: String = "",
  val twitterProfile: String = "",
  val writing: Boolean = false
)

enum class ManagePeopleMode {
  CREATE,
  EDIT
}

@HiltViewModel
class ManagePeopleViewModel @Inject constructor(
  private val peopleRepository: PeopleRepository
) : ViewModel() {
  private val _uiState = MutableStateFlow(ManagePeopleState())
  val uiState: StateFlow<ManagePeopleState> = _uiState.asStateFlow()

  fun onNameChanged(newName: String) = viewModelScope.launch {
    _uiState.update { it.copy(name = newName) }
  }

  fun onDescriptionChanged(newDescription: String) = viewModelScope.launch {
    _uiState.update { it.copy(description = newDescription) }
  }

  fun onCountryChanged(newCountry: String) = viewModelScope.launch {
    _uiState.update { it.copy(country = newCountry) }
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
        country = "",
        website = "",
        instagramProfile = "",
        twitterProfile = ""
      )
    }
  }

  fun setFieldValues(person: Person) = viewModelScope.launch {
    _uiState.update {
      it.copy(
        id = person.id,
        name = person.name,
        description = person.description.orEmpty(),
        country = person.country.orEmpty(),
        website = person.website.orEmpty(),
        instagramProfile = person.instagram_profile.orEmpty(),
        twitterProfile = person.twitter_profile.orEmpty()
      )
    }
  }

  private fun validate(): Boolean = uiState.value.name.isNotEmpty()

  fun create() = viewModelScope.launch {
    if (!validate()) {
      return@launch
    }

    _uiState.update { it.copy(writing = true) }

    peopleRepository.insert(
      name = uiState.value.name,
      description = uiState.value.description,
      country = uiState.value.country,
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

    peopleRepository.update(
      id = uiState.value.id!!,
      name = uiState.value.name,
      description = uiState.value.description,
      country = uiState.value.country,
      website = uiState.value.website,
      instagramProfile = uiState.value.instagramProfile,
      twitterProfile = uiState.value.twitterProfile,
    )

    clearFields()
    _uiState.update { it.copy(writing = false) }
  }
}