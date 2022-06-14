package io.github.alessandrojean.toshokan.presentation.ui.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.database.data.Book
import io.github.alessandrojean.toshokan.database.data.BookGroup
import io.github.alessandrojean.toshokan.database.data.Person
import io.github.alessandrojean.toshokan.database.data.Publisher
import io.github.alessandrojean.toshokan.database.data.Store
import io.github.alessandrojean.toshokan.domain.DateRange
import io.github.alessandrojean.toshokan.domain.SearchFilters
import io.github.alessandrojean.toshokan.repository.BooksRepository
import io.github.alessandrojean.toshokan.repository.GroupsRepository
import io.github.alessandrojean.toshokan.repository.PeopleRepository
import io.github.alessandrojean.toshokan.repository.PublishersRepository
import io.github.alessandrojean.toshokan.repository.StoresRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SearchState {
  HISTORY,
  NO_RESULTS_FOUND,
  RESULTS
}

@HiltViewModel
class SearchViewModel @Inject constructor(
  private val booksRepository: BooksRepository,
  groupsRepository: GroupsRepository,
  peopleRepository: PeopleRepository,
  publishersRepository: PublishersRepository,
  storesRepository: StoresRepository
) : ViewModel() {

  var filters by mutableStateOf(SearchFilters())
    private set
  val results = mutableStateListOf<Book>()
  var state by mutableStateOf(SearchState.HISTORY)
    private set

  val allGroups = groupsRepository.groupsSorted
  val allPublishers = publishersRepository.publishers
  val allPersons = peopleRepository.people
  val allStores = storesRepository.stores

  fun clearSearch() {
    state = SearchState.HISTORY
    filters = SearchFilters()
    results.clear()
  }

  fun onSearchTextChanged(newQuery: String) {
    filters = filters.copy(query = newQuery)
  }

  fun onIsFutureChanged(newIsFuture: Boolean?) {
    filters = filters.copy(isFuture = newIsFuture)
    search()
  }

  fun onFavoritesOnlyChanged(newFavoritesOnly: Boolean) {
    filters = filters.copy(favoritesOnly = newFavoritesOnly)
    search()
  }

  fun onGroupsChanged(newGroups: List<BookGroup>) {
    filters = filters.copy(groups = newGroups)
    search()
  }

  fun onPublishersChanged(newPublishers: List<Publisher>) {
    filters = filters.copy(publishers = newPublishers)
    search()
  }

  fun onStoresChanged(newStores: List<Store>) {
    filters = filters.copy(stores = newStores)
    search()
  }

  fun onContributorsChanged(newContributors: List<Person>) {
    filters = filters.copy(contributors = newContributors)
    search()
  }

  fun onBoughtAtChanged(newBoughtAt: DateRange?) {
    filters = filters.copy(boughtAt = newBoughtAt)
    search()
  }

  fun onReadAtChanged(newReadAt: DateRange?) {
    filters = filters.copy(readAt = newReadAt)
    search()
  }

  fun search() = viewModelScope.launch {
    results.clear()
    results.addAll(booksRepository.search(filters))

    state = if (results.isEmpty()) SearchState.NO_RESULTS_FOUND else SearchState.RESULTS
  }

}