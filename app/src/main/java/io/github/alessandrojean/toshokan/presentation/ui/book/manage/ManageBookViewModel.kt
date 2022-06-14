package io.github.alessandrojean.toshokan.presentation.ui.book.manage

import android.icu.util.Currency
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.BookGroup
import io.github.alessandrojean.toshokan.database.data.CompleteBook
import io.github.alessandrojean.toshokan.database.data.Publisher
import io.github.alessandrojean.toshokan.database.data.Store
import io.github.alessandrojean.toshokan.domain.Contributor
import io.github.alessandrojean.toshokan.domain.Price
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
import io.github.alessandrojean.toshokan.util.extension.toTitleParts
import kotlinx.coroutines.launch
import logcat.logcat
import java.util.Date
import java.util.Locale
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

  enum class Mode {
    CREATING,
    EDITING
  }

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
  var dimensionWidth by mutableStateOf("")
  var dimensionHeight by mutableStateOf("")

  val informationTabInvalid by derivedStateOf {
    code.isEmpty() || title.isEmpty() || publisherText.isEmpty() ||
      labelPriceValue.isEmpty() || labelPriceValue.parseLocaleValueOrNull() == null ||
      paidPriceValue.isEmpty() || paidPriceValue.parseLocaleValueOrNull() == null ||
      dimensionWidth.isEmpty() || dimensionWidth.parseLocaleValueOrNull() == null ||
      dimensionHeight.isEmpty() || dimensionHeight.parseLocaleValueOrNull() == null
  }

  val contributorsTabInvalid by derivedStateOf { contributors.isEmpty() }

  val organizationTabInvalid by derivedStateOf {
    storeText.isEmpty() || groupText.isEmpty()
  }

  val contributors = mutableStateListOf<Contributor>()

  private var lookupBook by mutableStateOf<LookupBookResult?>(null)
  private var existingBook by mutableStateOf<CompleteBook?>(null)
  var allCovers = mutableStateListOf<CoverResult>()
    private set
  var coverState by mutableStateOf<CoverTabState>(CoverTabState.Display)
    private set

  var mode by mutableStateOf(Mode.CREATING)
    private set

  val publishers = publishersRepository.publishers
  val stores = storesRepository.stores
  val groups = groupsRepository.groupsSorted
  val people = peopleRepository.people

  var selectedContributor by mutableStateOf<Contributor?>(null)

  var writing by mutableStateOf(false)
    private set

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

    if (lookupBook.dimensions.size == 2) {
      val (width, height) = lookupBook.dimensions
      dimensionWidth = width.toLocaleString()
      dimensionHeight = height.toLocaleString()
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

    mode = Mode.CREATING
  }

  fun setFieldValues(existingBook: CompleteBook) = viewModelScope.launch {
    if (existingBook == this@ManageBookViewModel.existingBook) {
      return@launch
    }

    code = existingBook.code.orEmpty()
    title = existingBook.title
    synopsis = existingBook.synopsis.orEmpty()
    publisher = publishersRepository.findById(existingBook.publisher_id)
    publisherText = existingBook.publisher_name
    store = existingBook.store_id?.let { storesRepository.findById(it) }
    storeText = existingBook.store_name
    group = groupsRepository.findById(existingBook.group_id)
    groupText = existingBook.group_name
    coverUrl = existingBook.cover_url.orEmpty()
    notes = existingBook.notes.orEmpty()
    isFuture = existingBook.is_future
    boughtAt = existingBook.bought_at

    labelPriceCurrency = existingBook.label_price_currency
    labelPriceValue = existingBook.label_price_value.toLocaleString()

    paidPriceCurrency = existingBook.paid_price_currency
    paidPriceValue = existingBook.paid_price_value.toLocaleString()

    dimensionWidth = existingBook.dimension_width.toLocaleString()
    dimensionHeight = existingBook.dimension_height.toLocaleString()

    this@ManageBookViewModel.existingBook = existingBook

    val allPeople = peopleRepository.selectAll()

    booksRepository.findBookContributors(existingBook.id)
      .map { bookContributor ->
        Contributor(
          person = allPeople.firstOrNull { it.id == bookContributor.person_id },
          personId = bookContributor.person_id,
          personText = bookContributor.person_name,
          role = bookContributor.role
        )
      }
      .let(contributors::addAll)

    if (existingBook.cover_url.orEmpty().isNotBlank()) {
      allCovers.add(
        CoverResult(
          source = R.string.current_cover,
          imageUrl = existingBook.cover_url!!
        )
      )
    }

    mode = Mode.EDITING
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
    } else if (contributors.find { it.personText == contributor.personText && it.role == contributor.role } == null) {
      contributors.add(contributor)
    }

    selectedContributor = null
  }

  fun create(onFinish: (Long?) -> Unit = {}) {
    if (informationTabInvalid || contributorsTabInvalid || organizationTabInvalid) {
      return
    }

    viewModelScope.launch {
      writing = true

      // First step: create the relationships if needed.
      val publisherId = publisher?.id ?: publishersRepository.insert(publisherText)!!
      val storeId = store?.id ?: storesRepository.insert(storeText)!!
      val groupId = group?.id ?: groupsRepository.insert(groupText)!!

      // Second step: create the people that doesn't exist on the database.
      val insertedContributors = contributors.map { contributor ->
        if (contributor.person != null) {
          return@map contributor
        }

        contributor.copy(
          personId = peopleRepository.insert(contributor.personText)
        )
      }

      // Third step: Create the book.
      val bookId = booksRepository.insert(
        code = code,
        title = title,
        volume = title.toTitleParts().number,
        synopsis = synopsis,
        notes = notes,
        publisherId = publisherId,
        groupId = groupId,
        paidPrice = Price(
          currency = paidPriceCurrency,
          value = paidPriceValue.parseLocaleValueOrNull() ?: 0f
        ),
        labelPrice = Price(
          currency = labelPriceCurrency,
          value = labelPriceValue.parseLocaleValueOrNull() ?: 0f
        ),
        storeId = storeId,
        boughtAt = boughtAt,
        isFuture = isFuture,
        coverUrl = coverUrl.ifEmpty { null },
        dimensionWidth = dimensionWidth.parseLocaleValueOrNull() ?: 0f,
        dimensionHeight = dimensionHeight.parseLocaleValueOrNull() ?: 0f,
        contributors = insertedContributors
      )

      onFinish.invoke(bookId)
      writing = false
    }
  }

  fun edit(onFinish: () -> Unit = {}) {
    if (informationTabInvalid || contributorsTabInvalid || organizationTabInvalid) {
      return
    }

    viewModelScope.launch {
      writing = true

      // First step: create the relationships if needed.
      val publisherId = publisher?.id ?: publishersRepository.insert(publisherText)!!
      val storeId = store?.id ?: storesRepository.insert(storeText)!!
      val groupId = group?.id ?: groupsRepository.insert(groupText)!!

      // Second step: create the people that doesn't exist on the database.
      val insertedContributors = contributors.map { contributor ->
        if (contributor.person != null) {
          return@map contributor
        }

        contributor.copy(
          personId = peopleRepository.insert(contributor.personText)
        )
      }

      // Third step: Update the book.
      booksRepository.update(
        id = existingBook!!.id,
        code = code,
        title = title,
        volume = title.toTitleParts().number,
        synopsis = synopsis,
        notes = notes,
        publisherId = publisherId,
        groupId = groupId,
        paidPrice = Price(
          currency = paidPriceCurrency,
          value = paidPriceValue.parseLocaleValueOrNull() ?: 0f
        ),
        labelPrice = Price(
          currency = labelPriceCurrency,
          value = labelPriceValue.parseLocaleValueOrNull() ?: 0f
        ),
        storeId = storeId,
        boughtAt = boughtAt,
        isFuture = isFuture,
        coverUrl = coverUrl.ifEmpty { null },
        dimensionWidth = dimensionWidth.parseLocaleValueOrNull() ?: 0f,
        dimensionHeight = dimensionHeight.parseLocaleValueOrNull() ?: 0f,
        contributors = insertedContributors
      )

      onFinish.invoke()
      writing = false
    }
  }

}