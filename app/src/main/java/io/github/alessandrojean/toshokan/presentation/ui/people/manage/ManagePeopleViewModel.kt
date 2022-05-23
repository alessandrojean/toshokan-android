package io.github.alessandrojean.toshokan.presentation.ui.people.manage

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.database.data.Person
import io.github.alessandrojean.toshokan.repository.PeopleRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ManagePeopleMode {
  CREATE,
  EDIT
}

@HiltViewModel
class ManagePeopleViewModel @Inject constructor(
  private val peopleRepository: PeopleRepository
) : ViewModel() {

  var id by mutableStateOf<Long?>(null)
  var name by mutableStateOf("")
  var description by mutableStateOf("")
  var country by mutableStateOf("")
  var website by mutableStateOf("")
  var instagramProfile by mutableStateOf("")
  var twitterProfile by mutableStateOf("")
  var writing by mutableStateOf(false)

  private val formInvalid by derivedStateOf { name.isEmpty() }

  fun clearFields() = viewModelScope.launch {
    id = null
    name = ""
    description = ""
    country = ""
    website = ""
    instagramProfile = ""
    twitterProfile = ""
  }

  fun setFieldValues(person: Person) = viewModelScope.launch {
    id = person.id
    name = person.name
    description = person.description.orEmpty()
    country = person.country.orEmpty()
    website = person.website.orEmpty()
    instagramProfile = person.instagram_profile.orEmpty()
    twitterProfile = person.twitter_profile.orEmpty()
  }

  fun create() = viewModelScope.launch {
    if (formInvalid) {
      return@launch
    }

    writing = true

    peopleRepository.insert(
      name = name,
      description = description,
      country = country,
      website = website,
      instagramProfile = instagramProfile,
      twitterProfile = twitterProfile
    )

    clearFields()
    writing = false
  }

  fun edit() = viewModelScope.launch {
    if (formInvalid) {
      return@launch
    }

    writing = true

    peopleRepository.update(
      id = id!!,
      name = name,
      description = description,
      country = country,
      website = website,
      instagramProfile = instagramProfile,
      twitterProfile = twitterProfile,
    )

    clearFields()
    writing = false
  }
}