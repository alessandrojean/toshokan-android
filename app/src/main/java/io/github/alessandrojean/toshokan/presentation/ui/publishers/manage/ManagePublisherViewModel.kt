package io.github.alessandrojean.toshokan.presentation.ui.publishers.manage

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.database.data.Publisher
import io.github.alessandrojean.toshokan.repository.PublishersRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ManagePublisherMode {
  CREATE,
  EDIT
}

@HiltViewModel
class ManagePublisherViewModel @Inject constructor(
  private val publishersRepository: PublishersRepository
) : ViewModel() {

  var id by mutableStateOf<Long?>(null)
  var name by mutableStateOf("")
  var description by mutableStateOf("")
  var website by mutableStateOf("")
  var instagramProfile by mutableStateOf("")
  var twitterProfile by mutableStateOf("")
  var writing by mutableStateOf(false)

  private val formInvalid by derivedStateOf { name.isEmpty() }

  fun clearFields() = viewModelScope.launch {
    id = null
    name = ""
    description = ""
    website = ""
    instagramProfile = ""
    twitterProfile = ""
  }

  fun setFieldValues(publisher: Publisher) = viewModelScope.launch {
    id = publisher.id
    name = publisher.name
    description = publisher.description.orEmpty()
    website = publisher.website.orEmpty()
    instagramProfile = publisher.instagram_user.orEmpty()
    twitterProfile = publisher.twitter_user.orEmpty()
  }

  fun create() = viewModelScope.launch {
    if (formInvalid) {
      return@launch
    }

    writing = true

    publishersRepository.insert(
      name = name,
      description = description,
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

    publishersRepository.update(
      id = id!!,
      name = name,
      description = description,
      website = website,
      instagramProfile = instagramProfile,
      twitterProfile = twitterProfile,
    )

    clearFields()
    writing = false
  }
}