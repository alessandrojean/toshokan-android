package io.github.alessandrojean.toshokan.presentation.ui.book.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumedWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.BookContributor
import io.github.alessandrojean.toshokan.database.data.CompleteBook
import io.github.alessandrojean.toshokan.domain.CreditRole
import io.github.alessandrojean.toshokan.domain.DateRange
import io.github.alessandrojean.toshokan.domain.SearchFilters
import io.github.alessandrojean.toshokan.presentation.extensions.withTonalElevation
import io.github.alessandrojean.toshokan.presentation.ui.core.components.ExpandedIconButton
import io.github.alessandrojean.toshokan.presentation.ui.search.SearchScreen
import io.github.alessandrojean.toshokan.presentation.ui.theme.ModalBottomSheetExtraLargeShape
import io.github.alessandrojean.toshokan.util.extension.formatToLocaleDate
import io.github.alessandrojean.toshokan.util.extension.toLanguageDisplayName
import io.github.alessandrojean.toshokan.util.extension.toLocaleCurrencyString
import io.github.alessandrojean.toshokan.util.extension.toLocaleString
import io.github.alessandrojean.toshokan.util.extension.toTitleParts
import io.github.alessandrojean.toshokan.util.toIsbnInformation
import java.text.DateFormat

@Composable
fun BookInformation(
  modifier: Modifier = Modifier,
  book: CompleteBook?,
  contributors: List<BookContributor>,
  hasBookNeighbors: Boolean = false,
  color: Color = MaterialTheme.colorScheme.surface,
  tonalElevation: Dp = 6.dp,
  bottomPadding: Dp = 0.dp,
  bottomBarVisible: Boolean = false,
  onReadingClick: () -> Unit,
  onEditClick: () -> Unit,
  onDeleteClick: () -> Unit
) {
  val navigator = LocalNavigator.currentOrThrow
  var synopsisExpanded by remember { mutableStateOf(false) }
  var synopsisToggleable by remember { mutableStateOf(false) }
  var synopsisLayoutResultState by remember { mutableStateOf<TextLayoutResult?>(null) }
  val synopsisBackground = color.withTonalElevation(tonalElevation)

  val minContributors = 4
  var contributorsExpanded by remember { mutableStateOf(false) }
  val contributorsToggleable by remember(contributors) {
    derivedStateOf { contributors.size > minContributors }
  }
  val visibleContributors by remember(contributors, contributorsExpanded) {
    derivedStateOf {
      if (contributorsExpanded) {
        contributors
      } else {
        contributors.take(minContributors)
      }
    }
  }
  val contributorsButtonHeight = 58f
  val contributorsIconRotation by animateFloatAsState(if (contributorsExpanded) 180f else 0f)

  val toggleButtonOffset by animateFloatAsState(if (synopsisExpanded) 0f else -18f)
  val toggleIconRotation by animateFloatAsState(if (synopsisExpanded) 180f else 0f)
  val buttonRowCorner = 18.dp
  val buttonRowContentPadding = PaddingValues(all = 12.dp)
  val buttonRowContainerColor = MaterialTheme.colorScheme.surfaceVariant
    .withTonalElevation(tonalElevation)

  val bookAuthors by remember(contributors) {
    derivedStateOf {
      contributors.filter { it.role in CreditRole.AUTHOR_ROLES }
    }
  }

  val isbnInformation by remember(book?.code) {
    derivedStateOf {
      book?.code?.toIsbnInformation()
    }
  }

  val bookRead by remember(book?.reading_count) {
    derivedStateOf { (book?.reading_count ?: 0) > 0 }
  }

  LaunchedEffect(synopsisLayoutResultState) {
    if (synopsisLayoutResultState == null) {
      return@LaunchedEffect
    }

    if (!synopsisExpanded && synopsisLayoutResultState!!.hasVisualOverflow) {
      synopsisToggleable = true
    }
  }

  val consumeInsets = if (bottomBarVisible) {
    Modifier.consumedWindowInsets(WindowInsets.navigationBars)
  } else {
    Modifier
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
      Text(
        modifier = Modifier
          .padding(horizontal = 24.dp)
          .semantics { heading() },
        text = book?.title.orEmpty(),
        style = MaterialTheme.typography.titleLarge
      )
      Text(
        modifier = Modifier.padding(horizontal = 24.dp),
        text = bookAuthors.joinToString(", ") { it.person_name },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodyLarge.copy(
          color = LocalContentColor.current.copy(alpha = 0.8f)
        )
      )
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
      ) {
        ExpandedIconButton(
          modifier = Modifier.weight(1f),
          icon = if (bookRead) {
            Icons.Filled.Bookmarks
          } else {
            Icons.Outlined.Bookmarks
          },
          text = if (bookRead) {
            stringResource(R.string.book_read)
          } else {
            stringResource(R.string.book_unread)
          },
          colors = ButtonDefaults.textButtonColors(
            containerColor = buttonRowContainerColor,
            contentColor = LocalContentColor.current,
          ),
          shape = RoundedCornerShape(
            topStart = buttonRowCorner,
            bottomStart = buttonRowCorner
          ),
          contentPadding = buttonRowContentPadding,
          onClick = onReadingClick
        )
        ExpandedIconButton(
          modifier = Modifier.weight(1f),
          icon = Icons.Outlined.Edit,
          text = stringResource(R.string.action_edit),
          colors = ButtonDefaults.textButtonColors(
            containerColor = buttonRowContainerColor,
            contentColor = LocalContentColor.current
          ),
          shape = RectangleShape,
          contentPadding = buttonRowContentPadding,
          onClick = onEditClick
        )
        ExpandedIconButton(
          modifier = Modifier.weight(1f),
          icon = Icons.Outlined.Delete,
          text = stringResource(R.string.action_delete),
          colors = ButtonDefaults.textButtonColors(
            containerColor = buttonRowContainerColor,
            contentColor = LocalContentColor.current
          ),
          shape = RoundedCornerShape(
            topEnd = buttonRowCorner,
            bottomEnd = buttonRowCorner
          ),
          contentPadding = buttonRowContentPadding,
          onClick = onDeleteClick,
        )
      }
      if (book?.synopsis.orEmpty().isNotBlank()) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .toggleable(
              value = synopsisExpanded,
              onValueChange = { synopsisExpanded = it },
              enabled = synopsisToggleable,
              role = Role.Checkbox,
              indication = null,
              interactionSource = MutableInteractionSource()
            )
            .padding(
              bottom = if (synopsisToggleable) 4.dp else 12.dp,
              start = 24.dp,
              end = 24.dp
            )
            .animateContentSize()
        ) {
          Text(
            text = book?.synopsis.orEmpty().ifEmpty { stringResource(R.string.no_synopsis) },
            maxLines = if (synopsisExpanded) Int.MAX_VALUE else 4,
            onTextLayout = { synopsisLayoutResultState = it },
            overflow = TextOverflow.Clip,
            style = MaterialTheme.typography.bodyMedium.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontStyle = if (book?.synopsis.orEmpty().isEmpty()) FontStyle.Italic else FontStyle.Normal
            )
          )
          if (synopsisToggleable) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .offset(y = toggleButtonOffset.dp)
                .background(
                  Brush.verticalGradient(
                    0.0f to Color.Transparent,
                    0.2f to synopsisBackground.copy(alpha = 0.5f),
                    0.8f to synopsisBackground
                  )
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                modifier = Modifier.graphicsLayer(rotationX = toggleIconRotation),
                imageVector = Icons.Outlined.ExpandMore,
                contentDescription = null
              )
            }
          }
        }
      }
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
        if (book?.code.orEmpty().isNotBlank()) {
          BookInformationRow(
            label = stringResource(R.string.code),
            value = book?.code.orEmpty()
          )
        }
        BookInformationRow(
          label = stringResource(R.string.created_at),
          value = book?.created_at?.formatToLocaleDate() ?: ""
        )
        BookInformationRow(
          label = stringResource(R.string.updated_at),
          value = book?.updated_at?.formatToLocaleDate() ?: ""
        )
      }
      Text(
        modifier = Modifier
          .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 12.dp)
          .semantics { heading() },
        text = stringResource(R.string.contributors),
        style = MaterialTheme.typography.titleLarge
      )
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .animateContentSize()
      ) {
        visibleContributors.forEach { contributor ->
          BookContributorRow(
            contributor = contributor,
            onClick = {
              val searchFilters = SearchFilters.Incomplete(
                contributors = listOf(contributor.person_id)
              )
              navigator.push(SearchScreen(searchFilters))
            }
          )
        }
        if (contributorsToggleable) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(contributorsButtonHeight.dp)
          ) {
            if (!contributorsExpanded) {
              BookContributorRow(contributor = contributors[minContributors])
            }
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(
                  Brush.verticalGradient(
                    0.0f to Color.Transparent,
                    0.2f to synopsisBackground.copy(alpha = 0.5f),
                    0.8f to synopsisBackground
                  )
                )
                .toggleable(
                  value = contributorsExpanded,
                  onValueChange = { contributorsExpanded = it },
                  role = Role.Checkbox,
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                modifier = Modifier.graphicsLayer(rotationX = contributorsIconRotation),
                imageVector = Icons.Outlined.ExpandMore,
                contentDescription = null
              )
            }
          }
        }
      }
      Text(
        modifier = Modifier
          .padding(
            start = 24.dp,
            end = 24.dp,
            top = 32.dp,
            bottom = 12.dp
          )
          .semantics { heading() },
        text = stringResource(R.string.metadata),
        style = MaterialTheme.typography.titleLarge
      )
      BookMetadataRow(
        label = stringResource(R.string.publisher),
        value = book?.publisher_name.orEmpty(),
        enabled = book != null,
        onClick = {
          val searchFilters = SearchFilters.Incomplete(
            publishers = listOf(book!!.publisher_id)
          )
          navigator.push(SearchScreen(searchFilters))
        }
      )
      if (hasBookNeighbors) {
        BookMetadataRow(
          label = stringResource(R.string.book_series),
          value = book?.title?.toTitleParts()?.title.orEmpty(),
          enabled = book != null,
          onClick = {
            val searchFilters = SearchFilters.Incomplete(
              collections = listOf(book!!.title.toTitleParts().title)
            )
            navigator.push(SearchScreen(searchFilters))
          }
        )
      }
      BookMetadataRow(
        label = stringResource(R.string.language),
        value = isbnInformation?.language?.toLanguageDisplayName().orEmpty(),
        enabled = false
      )
      BookMetadataRow(
        label = stringResource(R.string.group),
        value = book?.group_name.orEmpty(),
        enabled = book != null,
        onClick = {
          val searchFilters = SearchFilters.Incomplete(
            groups = listOf(book!!.group_id)
          )
          navigator.push(SearchScreen(searchFilters))
        }
      )
      BookMetadataRow(
        label = stringResource(R.string.dimensions),
        value = stringResource(
          R.string.dimensions_full,
          book?.dimension_width?.toLocaleString { maximumFractionDigits = 1 }.orEmpty(),
          book?.dimension_height?.toLocaleString { maximumFractionDigits = 1 }.orEmpty()
        ),
        enabled = false
      )
      BookMetadataRow(
        label = stringResource(R.string.label_price),
        value = book?.label_price_value?.toLocaleCurrencyString(book.label_price_currency).orEmpty(),
        enabled = false
      )
      BookMetadataRow(
        label = stringResource(R.string.paid_price),
        value = book?.paid_price_value?.toLocaleCurrencyString(book.paid_price_currency).orEmpty(),
        enabled = false
      )
      BookMetadataRow(
        label = stringResource(R.string.store),
        value = book?.store_name.orEmpty(),
        enabled = book != null,
        onClick = {
          val searchFilters = SearchFilters.Incomplete(
            stores = listOfNotNull(book!!.store_id)
          )
          navigator.push(SearchScreen(searchFilters))
        }
      )
      if (book?.bought_at != null) {
        BookMetadataRow(
          label = stringResource(R.string.bought_at),
          value = book.bought_at.formatToLocaleDate(format = DateFormat.LONG),
          onClick = {
            val searchFilters = SearchFilters.Incomplete(
              boughtAt = DateRange(start = book.bought_at, end = book.bought_at)
            )
            navigator.push(SearchScreen(searchFilters))
          }
        )
      }
      if (book?.latest_reading != null) {
        BookMetadataRow(
          label = stringResource(R.string.latest_reading),
          value = book.latest_reading.formatToLocaleDate(format = DateFormat.LONG),
          onClick = onReadingClick
        )
      }
      if (book?.notes.orEmpty().isNotBlank()) {
        Text(
          modifier = Modifier
            .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 12.dp)
            .semantics { heading() },
          text = stringResource(R.string.notes),
          style = MaterialTheme.typography.titleLarge
        )
        SelectionContainer(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
        ) {
          Text(
            text = book?.notes.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}
