package io.github.alessandrojean.toshokan.presentation.ui.library

import androidx.paging.PagingData
import androidx.paging.cachedIn
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import io.github.alessandrojean.toshokan.database.data.Book
import io.github.alessandrojean.toshokan.database.data.BookGroup
import io.github.alessandrojean.toshokan.repository.BooksRepository
import io.github.alessandrojean.toshokan.repository.GroupsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class LibraryScreenModel @Inject constructor(
  private val booksRepository: BooksRepository,
  groupsRepository: GroupsRepository
) : StateScreenModel<LibraryScreenModel.State>(State.Empty) {

  data class LibraryTab(
    val group: BookGroup,
    val books: Flow<PagingData<Book>>
  )

  sealed class State {
    object Empty : State()
    data class Library(val tabs: List<LibraryTab>) : State()
  }

  init {
    coroutineScope.launch {
      groupsRepository.subscribeGroupsNotEmpty().collect { groups ->
        mutableState.value = if (groups.isEmpty()) {
          State.Empty
        } else {
          State.Library(
            tabs = groups.map { group ->
              LibraryTab(
                group = group,
                books = booksRepository.groupBooksPaginated(group.id).cachedIn(coroutineScope)
              )
            }
          )
        }
      }
    }
  }

}