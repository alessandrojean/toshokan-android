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

  fun setFieldValues(publisher: Publisher) = viewModelScope.launch {
    id = publisher.id
    name = publisher.name
    description = publisher.description.orEmpty()
    website = publisher.website.orEmpty()
    instagramProfile = publisher.instagram_user.orEmpty()
    twitterProfile = publisher.twitter_user.orEmpty()
  }

  fun create(onFinish: () -> Unit = {}) = viewModelScope.launch {
    if (formInvalid) {
      return@launch
    }

    writing = true

    publishersRepository.insert(
      name = name.trim(),
      description = description.trim(),
      website = website.trim(),
      instagramProfile = instagramProfile.trim(),
      twitterProfile = twitterProfile.trim()
    )

    writing = false
    onFinish.invoke()
  }

  fun edit(onFinish: () -> Unit = {}) = viewModelScope.launch {
    if (formInvalid) {
      return@launch
    }

    writing = true

    publishersRepository.update(
      id = id!!,
      name = name.trim(),
      description = description.trim(),
      website = website.trim(),
      instagramProfile = instagramProfile.trim(),
      twitterProfile = twitterProfile.trim(),
    )

    writing = false
    onFinish.invoke()
  }
}