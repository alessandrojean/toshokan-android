package io.github.alessandrojean.toshokan.presentation.ui.book.manage

import android.icu.util.Currency
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.core.net.toUri
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import cafe.adriel.voyager.hilt.ScreenModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.alessandrojean.toshokan.data.cache.CoverCache
import io.github.alessandrojean.toshokan.data.preference.PreferencesManager
import io.github.alessandrojean.toshokan.database.data.BookGroup
import io.github.alessandrojean.toshokan.database.data.CompleteBook
import io.github.alessandrojean.toshokan.database.data.Publisher
import io.github.alessandrojean.toshokan.database.data.Store
import io.github.alessandrojean.toshokan.domain.Contributor
import io.github.alessandrojean.toshokan.domain.Price
import io.github.alessandrojean.toshokan.domain.RawTag
import io.github.alessandrojean.toshokan.presentation.ui.book.manage.components.CoverTabState
import io.github.alessandrojean.toshokan.repository.BooksRepository
import io.github.alessandrojean.toshokan.repository.GroupsRepository
import io.github.alessandrojean.toshokan.repository.PeopleRepository
import io.github.alessandrojean.toshokan.repository.PublishersRepository
import io.github.alessandrojean.toshokan.repository.StoresRepository
import io.github.alessandrojean.toshokan.repository.TagsRepository
import io.github.alessandrojean.toshokan.service.cover.BookCover
import io.github.alessandrojean.toshokan.service.cover.CoverRepository
import io.github.alessandrojean.toshokan.service.cover.SimpleBookInfo
import io.github.alessandrojean.toshokan.service.lookup.LookupBookResult
import io.github.alessandrojean.toshokan.util.extension.currentTime
import io.github.alessandrojean.toshokan.util.extension.parseLocaleValueOrNull
import io.github.alessandrojean.toshokan.util.extension.toLocaleString
import io.github.alessandrojean.toshokan.util.extension.toTitleParts
import kotlinx.coroutines.launch
import java.util.Date

class ManageBookScreenModel @AssistedInject constructor(
  private val booksRepository: BooksRepository,
  private val groupsRepository: GroupsRepository,
  private val peopleRepository: PeopleRepository,
  private val publishersRepository: PublishersRepository,
  private val storesRepository: StoresRepository,
  private val tagsRepository: TagsRepository,
  private val coverRepository: CoverRepository,
  private val coverCache: CoverCache,
  preferencesManager: PreferencesManager,
  @Assisted val lookupBook: LookupBookResult? = null,
  @Assisted val existingBookId: Long? = null
) : ScreenModel {

  @AssistedFactory
  interface Factory : ScreenModelFactory {
    fun create(
      @Assisted lookupBook: LookupBookResult? = null,
      @Assisted existingBookId: Long? = null
    ): ManageBookScreenModel
  }

  enum class Mode {
    CREATING,
    EDITING
  }

  val currency = preferencesManager.currency().getObject()

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
  var labelPriceCurrency by mutableStateOf(currency)
  var labelPriceValue by mutableStateOf("")
  var paidPriceCurrency by mutableStateOf(currency)
  var paidPriceValue by mutableStateOf("")
  var boughtAt by mutableStateOf<Long?>(currentTime)
  var isFuture by mutableStateOf(false)
  var cover by mutableStateOf<BookCover?>(null)
  var dimensionWidth by mutableStateOf("")
  var dimensionHeight by mutableStateOf("")
  var pageCount by mutableStateOf(0)
  var pageCountText by mutableStateOf("0")

  val informationTabInvalid by derivedStateOf {
    val conditions = listOf(
      code.isEmpty(),
      title.isEmpty(),
      publisherText.isEmpty(),
      labelPriceValue.isEmpty(),
      labelPriceValue.parseLocaleValueOrNull() == null,
      paidPriceValue.isEmpty(),
      paidPriceValue.parseLocaleValueOrNull() == null,
      dimensionWidth.isEmpty(),
      dimensionWidth.parseLocaleValueOrNull() == null,
      dimensionHeight.isEmpty(),
      dimensionHeight.parseLocaleValueOrNull() == null,
      pageCountText.toIntOrNull() == null
    )

    conditions.any { it }
  }

  val contributorsTabInvalid by derivedStateOf { contributors.isEmpty() }

  val organizationTabInvalid by derivedStateOf {
    storeText.isEmpty() || groupText.isEmpty()
  }

  val contributors = mutableStateListOf<Contributor>()

  var allCovers = mutableStateListOf<BookCover>()
    private set
  var coverState by mutableStateOf<CoverTabState>(CoverTabState.Display)
    private set

  val rawTags = mutableStateListOf<RawTag>()

  var mode by mutableStateOf(Mode.CREATING)
    private set

  val publishers = publishersRepository.publishers
  val stores = storesRepository.stores
  val groups = groupsRepository.groupsSorted
  val people = peopleRepository.people
  val tags = tagsRepository.subscribeToTags()

  var selectedContributor by mutableStateOf<Contributor?>(null)

  var writing by mutableStateOf(false)
    private set

  init {
    if (lookupBook != null) {
      setFieldValues(lookupBook)
    } else if (existingBookId != null) {
      booksRepository.findById(existingBookId)?.let { completeBook ->
        setFieldValues(completeBook)
      }
    }
  }

  // TODO: Make the view model use AssistedInject when Dagger adds support to it.
  private fun setFieldValues(lookupBook: LookupBookResult) = coroutineScope.launch {
    code = lookupBook.isbn
    title = lookupBook.title
    synopsis = lookupBook.synopsis
    publisherText = lookupBook.publisher
    pageCount = lookupBook.pageCount
    pageCountText = lookupBook.pageCount.toString()

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

    publishersRepository.selectAll()
      .firstOrNull { it.name.equals(lookupBook.publisher, ignoreCase = true) }
      ?.let { publisher = it }

    if (lookupBook.coverUrl.isNotBlank()) {
      cover = BookCover.Result(
        source = lookupBook.provider!!.title,
        imageUrl = lookupBook.coverUrl
      )
      allCovers.add(cover!!)
    }

    mode = Mode.CREATING
  }

  private fun setFieldValues(existingBook: CompleteBook) = coroutineScope.launch {
    code = existingBook.code.orEmpty()
    title = existingBook.title
    synopsis = existingBook.synopsis.orEmpty()
    publisher = publishersRepository.findById(existingBook.publisher_id)
    publisherText = existingBook.publisher_name
    store = existingBook.store_id?.let { storesRepository.findById(it) }
    storeText = existingBook.store_name
    group = groupsRepository.findById(existingBook.group_id)
    groupText = existingBook.group_name
    notes = existingBook.notes.orEmpty()
    isFuture = existingBook.is_future
    boughtAt = existingBook.bought_at
    pageCount = existingBook.page_count ?: 0
    pageCountText = existingBook.page_count?.toString() ?: "0"

    labelPriceCurrency = existingBook.label_price_currency
    labelPriceValue = existingBook.label_price_value.toLocaleString()

    paidPriceCurrency = existingBook.paid_price_currency
    paidPriceValue = existingBook.paid_price_value.toLocaleString()

    dimensionWidth = existingBook.dimension_width.toLocaleString()
    dimensionHeight = existingBook.dimension_height.toLocaleString()

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

    booksRepository.findBookTags(existingBook.id)
      .map { RawTag(it, it.id, it.name) }
      .let(rawTags::addAll)

    val customCover = coverCache.getCustomCoverFile(existingBook)

    if (customCover.exists()) {
      cover = BookCover.Custom(uri = customCover.toUri())
      allCovers.add(cover!!)
    } else if (existingBook.cover_url.orEmpty().isNotBlank()) {
      cover = BookCover.Current(imageUrl = existingBook.cover_url!!)
      allCovers.add(cover!!)
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

  fun fetchCovers() = coroutineScope.launch {
    if (code.isEmpty() || title.isEmpty() || publisherText.isEmpty()) {
      return@launch
    }

    if (coverState !is CoverTabState.Display) {
      return@launch
    }

    coverState = if (allCovers.isEmpty()) CoverTabState.Loading else CoverTabState.Refreshing

    val initialCovers = listOfNotNull(
      BookCover.Result(
        source = lookupBook?.provider?.title,
        imageUrl = lookupBook?.coverUrl.orEmpty()
      ),
      cover.takeIf { it is BookCover.Result }
    )

    val coversFound = coverRepository
      .find(
        SimpleBookInfo(
          code = code,
          title = title,
          publisher = publisherText,
          initialCovers = initialCovers
        )
      )
      .filterIsInstance<BookCover.Result>()

    allCovers = (allCovers.filterNot { it is BookCover.Result } + coversFound).toMutableStateList()

    val currentCover = allCovers.filterIsInstance<BookCover.Current>().firstOrNull()
    val duplicate = allCovers.filterIsInstance<BookCover.Result>()
      .firstOrNull { it.imageUrl == currentCover?.imageUrl }

    if (duplicate != null && currentCover != null) {
      allCovers.remove(currentCover)
      cover = duplicate
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

    coroutineScope.launch {
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

      // Third step: create the tags that doesn't exist on the database.
      val insertedTags = rawTags.map { rawTag ->
        if (rawTag.tag != null) {
          return@map rawTag
        }

        rawTag.copy(
          tagId = tagsRepository.insert(rawTag.tagText)
        )
      }

      // Fourth step: Create the book.
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
        pageCount = pageCountText.toIntOrNull() ?: 0,
        cover = cover,
        dimensionWidth = dimensionWidth.parseLocaleValueOrNull() ?: 0f,
        dimensionHeight = dimensionHeight.parseLocaleValueOrNull() ?: 0f,
        contributors = insertedContributors,
        tags = insertedTags
      )

      onFinish.invoke(bookId)
      writing = false
    }
  }

  fun edit(onFinish: () -> Unit = {}) {
    if (informationTabInvalid || contributorsTabInvalid || organizationTabInvalid) {
      return
    }

    coroutineScope.launch {
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

      // Third step: create the tags that doesn't exist on the database.
      val insertedTags = rawTags.map { rawTag ->
        if (rawTag.tag != null) {
          return@map rawTag
        }

        rawTag.copy(
          tagId = tagsRepository.insert(rawTag.tagText)
        )
      }

      // Third step: Update the book.
      booksRepository.update(
        id = existingBookId!!,
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
        pageCount = pageCountText.toIntOrNull() ?: 0,
        cover = cover,
        dimensionWidth = dimensionWidth.parseLocaleValueOrNull() ?: 0f,
        dimensionHeight = dimensionHeight.parseLocaleValueOrNull() ?: 0f,
        contributors = insertedContributors,
        tags = insertedTags
      )

      onFinish.invoke()
      writing = false
    }
  }

}