package io.github.alessandrojean.toshokan.presentation.ui.stores.manage

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.database.data.Store
import io.github.alessandrojean.toshokan.repository.StoresRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ManageStoreMode {
  CREATE,
  EDIT
}

@HiltViewModel
class ManageStoreViewModel @Inject constructor(
  private val storesRepository: StoresRepository
) : ViewModel() {

  var id by mutableStateOf<Long?>(null)
  var name by mutableStateOf("")
  var description by mutableStateOf("")
  var website by mutableStateOf("")
  var instagramProfile by mutableStateOf("")
  var twitterProfile by mutableStateOf("")
  var writing by mutableStateOf(false)

  private val formInvalid by derivedStateOf { name.isEmpty() }

  fun setFieldValues(store: Store) = viewModelScope.launch {
    id = store.id
    name = store.name
    description = store.description.orEmpty()
    website = store.website.orEmpty()
    instagramProfile = store.instagram_profile.orEmpty()
    twitterProfile = store.twitter_profile.orEmpty()
  }

  fun create(onFinish: () -> Unit = {}) = viewModelScope.launch {
    if (formInvalid) {
      return@launch
    }

    writing = true

    storesRepository.insert(
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

    storesRepository.update(
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