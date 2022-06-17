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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class SearchState {
  HISTORY,
  NO_RESULTS_FOUND,
  RESULTS
}

@HiltViewModel
class SearchViewModel @Inject constructor(
  private val booksRepository: BooksRepository,
  private val groupsRepository: GroupsRepository,
  private val peopleRepository: PeopleRepository,
  private val publishersRepository: PublishersRepository,
  private val storesRepository: StoresRepository
) : ViewModel() {

  var filters by mutableStateOf(SearchFilters.Complete())
    private set
  val results = mutableStateListOf<Book>()
  var state by mutableStateOf(SearchState.HISTORY)
    private set

  val allGroups = groupsRepository.groupsSorted
  val allPublishers = publishersRepository.publishers
  val allPersons = peopleRepository.people
  val allStores = storesRepository.stores
  val allCollections = booksRepository.findCollections()

  fun clearSearch() {
    state = SearchState.HISTORY
    filters = SearchFilters.Complete()
    results.clear()
  }

  fun onFiltersChanged(newFilters: SearchFilters) {
    if (newFilters is SearchFilters.Complete) {
      filters = newFilters.copy()
    } else if (newFilters is SearchFilters.Incomplete) {
      viewModelScope.launch {
        filters = withContext(Dispatchers.IO) {
          SearchFilters.Complete(
            query = newFilters.query,
            isFuture = newFilters.isFuture,
            favoritesOnly = newFilters.favoritesOnly,
            collections = newFilters.collections,
            groups = groupsRepository.findByIds(newFilters.groups),
            publishers = publishersRepository.findByIds(newFilters.publishers),
            contributors = peopleRepository.findByIds(newFilters.contributors),
            stores = storesRepository.findByIds(newFilters.stores),
            boughtAt = newFilters.boughtAt,
            readAt = newFilters.readAt
          )
        }
      }
    }

    search()
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

  fun onCollectionsChanged(newCollections: List<String>) {
    filters = filters.copy(collections = newCollections)
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