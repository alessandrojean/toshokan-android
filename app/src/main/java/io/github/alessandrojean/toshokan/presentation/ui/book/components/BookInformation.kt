package io.github.alessandrojean.toshokan.presentation.ui.book.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumedWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.BookContributor
import io.github.alessandrojean.toshokan.database.data.CompleteBook
import io.github.alessandrojean.toshokan.database.data.Tag
import io.github.alessandrojean.toshokan.domain.Collection
import io.github.alessandrojean.toshokan.domain.CreditRole
import io.github.alessandrojean.toshokan.domain.DateRange
import io.github.alessandrojean.toshokan.domain.DomainBook
import io.github.alessandrojean.toshokan.domain.DomainContributor
import io.github.alessandrojean.toshokan.domain.DomainTag
import io.github.alessandrojean.toshokan.domain.SearchFilters
import io.github.alessandrojean.toshokan.presentation.ui.search.SearchScreen
import io.github.alessandrojean.toshokan.presentation.ui.theme.ModalBottomSheetExtraLargeShape
import io.github.alessandrojean.toshokan.util.extension.formatToLocaleDate
import io.github.alessandrojean.toshokan.util.extension.push
import io.github.alessandrojean.toshokan.util.extension.toLanguageDisplayName
import io.github.alessandrojean.toshokan.util.extension.toLocaleCurrencyString
import io.github.alessandrojean.toshokan.util.extension.toLocaleString
import io.github.alessandrojean.toshokan.util.extension.toTitleParts
import io.github.alessandrojean.toshokan.util.toIsbnInformation
import java.text.DateFormat

@Composable
fun BookInformation(
  modifier: Modifier = Modifier,
  book: DomainBook?,
  contributors: List<DomainContributor>,
  tags: List<DomainTag>,
  hasBookNeighbors: Boolean = false,
  inLibrary: Boolean,
  color: Color = MaterialTheme.colorScheme.surface,
  tonalElevation: Dp = 6.dp,
  bottomPadding: Dp = 0.dp,
  bottomBarVisible: Boolean = false,
  onAddToLibraryClick: () -> Unit,
  onReadingClick: () -> Unit,
  onEditClick: () -> Unit,
  onDeleteClick: () -> Unit
) {
  val navigator = LocalNavigator.currentOrThrow
  val context = LocalContext.current

  val bookAuthors by remember(contributors) {
    derivedStateOf {
      contributors.filter { it.role in CreditRole.AUTHOR_ROLES }
    }
  }
  val isbnInformation by remember(book?.code) {
    derivedStateOf { book?.code?.toIsbnInformation() }
  }
  val bookRead by remember(book?.readingCount) {
    derivedStateOf { (book?.readingCount ?: 0) > 0 }
  }

  val consumeInsets = if (bottomBarVisible) {
    Modifier.consumedWindowInsets(WindowInsets.navigationBars)
  } else {
    Modifier
  }

  val createdAt = remember(book?.createdAt) { book?.createdAt?.formatToLocaleDate() }
  val updatedAt = remember(book?.updatedAt) { book?.updatedAt?.formatToLocaleDate() }
  val titleParts = remember(book?.title) { book?.title?.toTitleParts() }

  var synopsisToggleable by remember { mutableStateOf(false) }
  var synopsisExpanded by remember { mutableStateOf(false) }
  val synopsisIsEmpty = remember(book?.synopsis) { book?.synopsis.orEmpty().isBlank() }

  val tagsExpanded = remember(synopsisExpanded, synopsisToggleable, synopsisIsEmpty) {
    synopsisIsEmpty || (synopsisToggleable && synopsisExpanded)
  }

  Surface(
    modifier = modifier,
    shape = ModalBottomSheetExtraLargeShape,
    color = color,
    tonalElevation = tonalElevation
  ) {
    Column(
      modifier = Modifier
        .padding(
          top = 24.dp,
          bottom = bottomPadding
        )
        .then(consumeInsets)
        .navigationBarsPadding()
    ) {
      BookBasicInfo(
        placeholder = book == null,
        title = book?.title.orEmpty(),
        authors = remember(bookAuthors) {
          bookAuthors.joinToString(", ") { it.name!! }
        },
        isRead = bookRead,
        isFuture = book?.isFuture == true,
        inLibrary = inLibrary,
        onAddToLibraryClick = onAddToLibraryClick,
        onReadingClick = onReadingClick,
        onEditClick = onEditClick,
        onDeleteClick = onDeleteClick
      )
      BookSynopsis(
        synopsis = book?.synopsis,
        containerColor = color,
        tonalElevation = tonalElevation,
        onSynopsisExpandedChange = { synopsisExpanded = it },
        onSynopsisToggleableChange = { synopsisToggleable = it }
      )
      BookTags(
        tags = tags,
        contentPadding = PaddingValues(
          start = 24.dp,
          end = 24.dp,
          top = if (tagsExpanded && !synopsisIsEmpty) 16.dp else 0.dp,
          bottom = if (tagsExpanded && !synopsisIsEmpty) 24.dp else 16.dp
        ),
        expanded = tagsExpanded,
        onTagClick = { tag ->
          navigator.push {
            SearchScreen(
              filters = SearchFilters.Incomplete(
                tags = listOfNotNull(tag.id)
              )
            )
          }
        }
      )
      Divider(
        modifier = Modifier.padding(
          bottom = 12.dp,
          start = 24.dp,
          end = 24.dp
        ),
        color = LocalContentColor.current.copy(alpha = 0.2f)
      )
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
      ) {
        if (book?.code.orEmpty().isNotBlank() || book == null) {
          BookInformationRow(
            label = stringResource(R.string.code),
            value = book?.code.orEmpty()
          )
        }
        BookInformationRow(
          label = stringResource(R.string.created_at),
          value = createdAt ?: ""
        )
        BookInformationRow(
          label = stringResource(R.string.updated_at),
          value = updatedAt ?: ""
        )
      }
      BookContributors(
        contributors = contributors,
        inLibrary = inLibrary,
        containerColor = color,
        tonalElevation = tonalElevation,
        onContributorClick = { contributor ->
          navigator.push {
            SearchScreen(
              filters = SearchFilters.Incomplete(
                contributors = listOfNotNull(contributor.personId)
              )
            )
          }
        }
      )
      BookMetadata(
        enabled = book != null,
        inLibrary = inLibrary,
        publisher = book?.publisher?.title.orEmpty(),
        hasBookNeighbors = hasBookNeighbors,
        series = titleParts?.title,
        language = remember(isbnInformation) {
          isbnInformation?.language?.toLanguageDisplayName()
        },
        pageCount = book?.pageCount?.takeIf { it > 0 },
        group = book?.group?.title.orEmpty(),
        dimensions = remember(book?.dimensions?.width, book?.dimensions?.height) {
          context.getString(
            R.string.dimensions_full,
            book?.dimensions?.width?.toLocaleString { maximumFractionDigits = 1 }.orEmpty(),
            book?.dimensions?.height?.toLocaleString { maximumFractionDigits = 1 }.orEmpty()
          )
        },
        labelPrice = remember(book?.labelPrice?.currency, book?.labelPrice?.value) {
          book?.labelPrice?.value?.toLocaleCurrencyString(book.labelPrice.currency).orEmpty()
        },
        paidPrice = remember(book?.paidPrice?.currency, book?.paidPrice?.value) {
          book?.paidPrice?.value?.toLocaleCurrencyString(book.paidPrice.currency).orEmpty()
        },
        store = book?.store?.title.orEmpty(),
        boughtAt = remember(book?.boughtAt) {
          book?.boughtAt?.formatToLocaleDate(format = DateFormat.LONG)
        },
        latestReading = remember(book?.latestReading) {
          book?.latestReading?.formatToLocaleDate(format = DateFormat.LONG)
        },
        onPublisherClick = {
          navigator.push {
            SearchScreen(
              filters = SearchFilters.Incomplete(
                publishers = listOfNotNull(book!!.publisher.id)
              )
            )
          }
        },
        onSeriesClick = {
          navigator.push {
            SearchScreen(
              filters = SearchFilters.Incomplete(
                collections = listOf(
                  Collection(title = titleParts!!.title, groupId = book!!.group.id)
                )
              )
            )
          }
        },
        onGroupClick = {
          navigator.push {
            SearchScreen(
              filters = SearchFilters.Incomplete(
                groups = listOfNotNull(book!!.group.id)
              )
            )
          }
        },
        onStoreClick = {
          navigator.push {
            SearchScreen(
              filters = SearchFilters.Incomplete(
                stores = listOfNotNull(book!!.store.id)
              )
            )
          }
        },
        onBoughtAtClick = {
          if (book?.boughtAt != null) {
            navigator.push {
              SearchScreen(
                filters = SearchFilters.Incomplete(
                  boughtAt = DateRange(start = book.boughtAt, end = book.boughtAt)
                )
              )
            }
          }
        },
        onLatestReadingClick = onReadingClick
      )
      BookNotes(notes = book?.notes)
    }
  }
}

