package io.github.alessandrojean.toshokan.presentation.ui.book.manage

import android.icu.util.Currency
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.BookGroup
import io.github.alessandrojean.toshokan.database.data.Publisher
import io.github.alessandrojean.toshokan.database.data.Store
import io.github.alessandrojean.toshokan.domain.Contributor
import io.github.alessandrojean.toshokan.presentation.ui.book.manage.components.CoverTabState
import io.github.alessandrojean.toshokan.repository.BooksRepository
import io.github.alessandrojean.toshokan.repository.GroupsRepository
import io.github.alessandrojean.toshokan.repository.PeopleRepository
import io.github.alessandrojean.toshokan.repository.PublishersRepository
import io.github.alessandrojean.toshokan.repository.StoresRepository
import io.github.alessandrojean.toshokan.service.cover.CoverRepository
import io.github.alessandrojean.toshokan.service.cover.CoverResult
import io.github.alessandrojean.toshokan.service.cover.SimpleBookInfo
import io.github.alessandrojean.toshokan.service.lookup.LookupBookResult
import io.github.alessandrojean.toshokan.util.extension.parseLocaleValueOrNull
import io.github.alessandrojean.toshokan.util.extension.toLocaleString
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.launch
import logcat.logcat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ManageBookViewModel @Inject constructor(
  private val booksRepository: BooksRepository,
  private val groupsRepository: GroupsRepository,
  private val peopleRepository: PeopleRepository,
  private val publishersRepository: PublishersRepository,
  private val storesRepository: StoresRepository,
  private val coverRepository: CoverRepository
) : ViewModel() {

  var id by mutableStateOf<Long?>(null)
  var code by mutableStateOf("")
  var title by mutableStateOf("")
  var synopsis by mutableStateOf("")
  var notes by mutableStateOf("")
  var publisherText by mutableStateOf("")
  var publisher by mutableStateOf<Publisher?>(null)
  var storeText by mutableStateOf("")
  var store by mutableStateOf<Store?>(null)
  var groupText by mutableStateOf("")
  var group by mutableStateOf<BookGroup?>(null)
  var labelPriceCurrency by mutableStateOf<Currency>(Currency.getInstance(Locale.getDefault()))
  var labelPriceValue by mutableStateOf("")
  var paidPriceCurrency by mutableStateOf<Currency>(Currency.getInstance(Locale.getDefault()))
  var paidPriceValue by mutableStateOf("")
  var boughtAt by mutableStateOf<Long?>(Date().time)
  var isFuture by mutableStateOf(false)
  var coverUrl by mutableStateOf("")

  val informationTabInvalid by derivedStateOf {
    code.isEmpty() || title.isEmpty() || publisherText.isEmpty() ||
      labelPriceValue.isEmpty() || labelPriceValue.parseLocaleValueOrNull() == null ||
      paidPriceValue.isEmpty() || paidPriceValue.parseLocaleValueOrNull() == null
  }

  val contributorsTabInvalid by derivedStateOf { contributors.isEmpty() }

  val organizationTabInvalid by derivedStateOf {
    storeText.isEmpty() || groupText.isEmpty()
  }

  val contributors = mutableStateListOf<Contributor>()

  private var lookupBook by mutableStateOf<LookupBookResult?>(null)
  var allCovers = mutableStateListOf<CoverResult>()
    private set
  var coverState by mutableStateOf<CoverTabState>(CoverTabState.Display)
    private set

  val publishers = publishersRepository.publishers
  val stores = storesRepository.stores
  val groups = groupsRepository.groupsSorted
  val people = peopleRepository.people

  var selectedContributor by mutableStateOf<Contributor?>(null)

  // TODO: Make the view model use AssistedInject when Dagger adds support to it.
  fun setFieldValues(lookupBook: LookupBookResult) = viewModelScope.launch {
    if (lookupBook == this@ManageBookViewModel.lookupBook) {
      return@launch
    }

    code = lookupBook.isbn
    title = lookupBook.title
    synopsis = lookupBook.synopsis
    publisherText = lookupBook.publisher
    coverUrl = lookupBook.coverUrl

    lookupBook.labelPrice?.let {
      labelPriceCurrency = it.currency
      labelPriceValue = it.value.toLocaleString()
    }

    val allPeople = peopleRepository.selectAll()

    lookupBook.contributors
      .map { contributor ->
        val person = allPeople.firstOrNull { it.name.equals(contributor.name, ignoreCase = true) }
        Contributor(
          person = person,
          personText = person?.name ?: contributor.name,
          role = contributor.role
        )
      }
      .let(contributors::addAll)

    logcat { contributors.toString() }

    this@ManageBookViewModel.lookupBook = lookupBook

    publishersRepository.selectAll()
      .firstOrNull { it.name.equals(lookupBook.publisher, ignoreCase = true) }
      ?.let { publisher = it }

    if (lookupBook.coverUrl.isNotBlank()) {
      allCovers.add(
        CoverResult(
          source = lookupBook.provider!!.title,
          imageUrl = lookupBook.coverUrl
        )
      )
    }
  }

  fun coverRefreshEnabled(): Boolean {
    return coverRepository.hasProvider(
      SimpleBookInfo(
        code = code,
        title = title,
        publisher = publisherText
      )
    )
  }

  fun fetchCovers() = viewModelScope.launch {
    if (code.isEmpty() || title.isEmpty() || publisherText.isEmpty()) {
      return@launch
    }

    if (coverState !is CoverTabState.Display) {
      return@launch
    }

    coverState = if (allCovers.isEmpty()) CoverTabState.Loading else CoverTabState.Refreshing

    val initialCovers = mutableListOf(
      CoverResult(
        source = lookupBook?.provider?.title,
        imageUrl = lookupBook?.coverUrl.orEmpty()
      )
    )

    if (
      lookupBook?.coverUrl.orEmpty() != coverUrl &&
        allCovers.firstOrNull { it.imageUrl == coverUrl } == null
    ) {
      initialCovers.add(
        CoverResult(
          source = R.string.current_cover,
          imageUrl = coverUrl
        )
      )
    }

    allCovers = coverRepository
      .find(
        SimpleBookInfo(
          code = code,
          title = title,
          publisher = publisherText,
          initialCovers = initialCovers
        )
      )
      .toMutableStateList()

    val currentCover = allCovers.firstOrNull { it.imageUrl == coverUrl }

    if (currentCover == null) {
      coverUrl = ""
    }

    coverState = CoverTabState.Display
  }

  fun removeSelectedContributor() {
    contributors.remove(selectedContributor)
    selectedContributor = null
  }

  fun handleContributor(contributor: Contributor) {
    if (selectedContributor != null) {
      val currentIndex = contributors.indexOf(selectedContributor)
      contributors[currentIndex] = contributor
    } else {
      contributors.add(contributor)
    }

    selectedContributor = null
  }

}