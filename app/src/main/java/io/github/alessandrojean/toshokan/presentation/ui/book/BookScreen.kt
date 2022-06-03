package io.github.alessandrojean.toshokan.presentation.ui.book

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.BookContributor
import io.github.alessandrojean.toshokan.database.data.CompleteBook
import io.github.alessandrojean.toshokan.domain.CreditRole
import io.github.alessandrojean.toshokan.presentation.extensions.surfaceWithTonalElevation
import io.github.alessandrojean.toshokan.presentation.ui.book.manage.ManageBookScreen
import io.github.alessandrojean.toshokan.presentation.ui.core.components.ExpandedIconButton
import io.github.alessandrojean.toshokan.presentation.ui.theme.DividerOpacity
import io.github.alessandrojean.toshokan.presentation.ui.theme.ModalBottomSheetShape
import io.github.alessandrojean.toshokan.util.extension.formatToLocaleDate
import io.github.alessandrojean.toshokan.util.extension.toLanguageDisplayName
import io.github.alessandrojean.toshokan.util.extension.toLocaleCurrencyString
import io.github.alessandrojean.toshokan.util.extension.toLocaleString
import io.github.alessandrojean.toshokan.util.toIsbnInformation
import java.lang.Float.min
import java.text.DateFormat
import kotlin.math.ceil

data class BookScreen(val bookId: Long) : AndroidScreen() {

  @Composable
  @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
  override fun Content() {
    val bookViewModel = getViewModel<BookViewModel>()
    val book by bookViewModel.findTheBook(bookId).collectAsState(initial = null)
    val bookContributors by bookViewModel.findTheBookContributors(bookId).collectAsState(emptyList())
    val navigator = LocalNavigator.currentOrThrow

    val scrollState = rememberScrollState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior() }

    val systemBarColor = Color.Transparent
    val systemBarDarkIcons = !isSystemInDarkTheme()
    val systemUiController = rememberSystemUiController()

    val scrolledTopBarContainerColor = MaterialTheme.colorScheme.surfaceWithTonalElevation(6.dp)
    val maxPoint = with(LocalDensity.current) {
      LocalConfiguration.current.screenWidthDp.dp.toPx() * 0.8f
    }
    val currentScrollBounded = min(scrollState.value.toFloat(), maxPoint)
    val topBarTitleOpacity = currentScrollBounded / maxPoint
    val coverBottomOffsetDp = 18f

    val topBarContainerColor by remember(topBarTitleOpacity) {
      derivedStateOf {
        scrolledTopBarContainerColor.copy(alpha = topBarTitleOpacity)
      }
    }

    SideEffect {
      systemUiController.setStatusBarColor(
        color = systemBarColor,
        darkIcons = systemBarDarkIcons
      )
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    DeleteDialog(
      visible = showDeleteDialog,
      onDismiss = { showDeleteDialog = false },
      onDelete = {
        bookViewModel.delete()
        navigator.pop()
      }
    )

    Scaffold(
      modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
      topBar = {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          color = topBarContainerColor,
          tonalElevation = 0.dp
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .statusBarsPadding()
          ) {
            SmallTopAppBar(
              scrollBehavior = scrollBehavior,
              colors = TopAppBarDefaults.smallTopAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent
              ),
              navigationIcon = {
                IconButton(onClick = { navigator.pop() }) {
                  Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.action_back)
                  )
                }
              },
              title = {
                Text(
                  modifier = Modifier.graphicsLayer(alpha = topBarTitleOpacity),
                  text = book?.title.orEmpty(),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              },
              actions = {
                IconButton(onClick = { }) {
                  Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = stringResource(R.string.action_edit)
                  )
                }
              }
            )
            Divider(
              modifier = Modifier
                .graphicsLayer(alpha = topBarTitleOpacity),
              color = LocalContentColor.current.copy(alpha = DividerOpacity)
            )
          }
        }
      },
      content = {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
        ) {
          BookCoverBox(
            modifier = Modifier
              .fillMaxWidth()
              .offset(y = (ceil(100f * topBarTitleOpacity)).dp),
            coverUrl = book?.cover_url.orEmpty(),
            contentDescription = book?.title,
            bottomOffsetDp = coverBottomOffsetDp,
            topBarHeightDp = 52f,
          )
          BookInformation(
            modifier = Modifier
              .offset(y = (-coverBottomOffsetDp).dp)
              .fillMaxWidth(),
            book = book,
            contributors = bookContributors,
            onEditClick = {
              if (book != null) {
                navigator.push(
                  ManageBookScreen(completeBook = book)
                )
              }
            },
            onDeleteClick = { showDeleteDialog = true }
          )
        }
      }
    )
  }

  @Composable
  fun BookCoverBox(
    modifier: Modifier = Modifier,
    coverUrl: String,
    contentDescription: String?,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    topBarHeightDp: Float = 64f,
    bottomOffsetDp: Float = 18f
  ) {
    val verticalPadding = (topBarHeightDp + bottomOffsetDp).dp +
      WindowInsets.statusBars
        .asPaddingValues()
        .calculateTopPadding()

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f / 1.15f)
        .background(MaterialTheme.colorScheme.surface)
        .then(modifier),
      contentAlignment = Alignment.Center
    ) {
      AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
          .data(coverUrl)
          .crossfade(true)
          .build(),
        modifier = Modifier
          .fillMaxSize()
          .graphicsLayer(alpha = 0.3f),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
      )
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              0.0f to containerColor,
              1.0f to containerColor.copy(alpha = 0.2f)
            )
          )
      )
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(
            top = verticalPadding,
            bottom = (topBarHeightDp + bottomOffsetDp).dp,
            start = 32.dp,
            end = 32.dp
          )
          .clipToBounds(),
        contentAlignment = Alignment.Center
      ) {
        AsyncImage(
          model = ImageRequest.Builder(LocalContext.current)
            .data(coverUrl)
            .crossfade(true)
            .build(),
          modifier = Modifier.clip(MaterialTheme.shapes.large),
          contentDescription = contentDescription,
          contentScale = ContentScale.Inside,
        )
      }
    }
  }

  @Composable
  fun BookInformation(
    modifier: Modifier = Modifier,
    book: CompleteBook?,
    contributors: List<BookContributor>,
    containerColor: Color = MaterialTheme.colorScheme.surfaceWithTonalElevation(6.dp),
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
  ) {
    var synopsisExpanded by remember { mutableStateOf(false) }
    var synopsisToggleable by remember { mutableStateOf(false) }
    var synopsisLayoutResultState by remember { mutableStateOf<TextLayoutResult?>(null) }

    val toggleButtonOffset by animateFloatAsState(if (synopsisExpanded) 0f else -18f)
    val toggleIconRotation by animateFloatAsState(if (synopsisExpanded) 180f else 0f)

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

    LaunchedEffect(synopsisLayoutResultState) {
      if (synopsisLayoutResultState == null) {
        return@LaunchedEffect
      }

      if (!synopsisExpanded && synopsisLayoutResultState!!.hasVisualOverflow) {
        synopsisToggleable = true
      }
    }

    Column(
      modifier = modifier
        .clip(ModalBottomSheetShape)
        .background(containerColor)
        .padding(top = 24.dp)
        .navigationBarsPadding()
    ) {
      Text(
        modifier = Modifier.padding(horizontal = 24.dp),
        text = book?.title.orEmpty(),
        style = MaterialTheme.typography.titleLarge
      )
      Text(
        modifier = Modifier.padding(horizontal = 24.dp),
        text = bookAuthors.joinToString(", ") { it.person_name },
        style = MaterialTheme.typography.bodyLarge.copy(
          color = LocalContentColor.current.copy(alpha = 0.8f)
        )
      )
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 24.dp, end = 24.dp, top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        ExpandedIconButton(
          modifier = Modifier.weight(1f),
          icon = Icons.Filled.BookmarkAdd,
          text = stringResource(R.string.book_read),
          contentColor = MaterialTheme.colorScheme.primary,
          onClick = { /*TODO*/ }
        )
        ExpandedIconButton(
          modifier = Modifier.weight(1f),
          icon = Icons.Outlined.Edit,
          text = stringResource(R.string.action_edit),
          onClick = onEditClick
        )
        ExpandedIconButton(
          modifier = Modifier.weight(1f),
          icon = Icons.Outlined.Delete,
          text = stringResource(R.string.action_delete),
          onClick = onDeleteClick
        )
      }
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
            top = 12.dp,
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
          overflow = TextOverflow.Ellipsis,
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
                  0.2f to containerColor.copy(alpha = 0.5f),
                  0.8f to containerColor
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
          InformationRow(
            label = stringResource(R.string.code),
            value = book?.code.orEmpty()
          )
        }
        InformationRow(
          label = stringResource(R.string.created_at),
          value = book?.created_at?.formatToLocaleDate(format = DateFormat.LONG) ?: ""
        )
        InformationRow(
          label = stringResource(R.string.updated_at),
          value = book?.updated_at?.formatToLocaleDate(format = DateFormat.LONG) ?: ""
        )
      }
      Text(
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 12.dp),
        text = stringResource(R.string.contributors),
        style = MaterialTheme.typography.titleLarge
      )
      contributors.forEach { contributor ->
        ContributorRow(
          contributor = contributor,
          onClick = { /* TODO */ }
        )
      }
      Text(
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 12.dp),
        text = stringResource(R.string.metadata),
        style = MaterialTheme.typography.titleLarge
      )
      MetadataRow(
        label = stringResource(R.string.publisher),
        value = book?.publisher_name.orEmpty(),
      )
      MetadataRow(
        label = stringResource(R.string.language),
        value = isbnInformation?.language?.toLanguageDisplayName().orEmpty(),
      )
      MetadataRow(
        label = stringResource(R.string.group),
        value = book?.group_name.orEmpty(),
      )
      MetadataRow(
        label = stringResource(R.string.dimensions),
        value = stringResource(
          R.string.dimensions_full,
          book?.dimension_width?.toLocaleString { maximumFractionDigits = 1 }.orEmpty(),
          book?.dimension_height?.toLocaleString { maximumFractionDigits = 1 }.orEmpty()
        ),
      )
      MetadataRow(
        label = stringResource(R.string.label_price),
        value = book?.label_price_value?.toLocaleCurrencyString(book.label_price_currency).orEmpty()
      )
      MetadataRow(
        label = stringResource(R.string.paid_price),
        value = book?.paid_price_value?.toLocaleCurrencyString(book.paid_price_currency).orEmpty()
      )
      MetadataRow(
        label = stringResource(R.string.store),
        value = book?.store_name.orEmpty(),
      )
      if (book?.bought_at != null) {
        MetadataRow(
          label = stringResource(R.string.bought_at),
          value = book.bought_at.formatToLocaleDate(format = DateFormat.LONG),
        )
      }
      if (book?.notes.orEmpty().isNotBlank()) {
        Text(
          modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 12.dp),
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

  @Composable
  fun ContributorRow(
    modifier: Modifier = Modifier,
    contributor: BookContributor,
    onClick: () -> Unit = {}
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(horizontal = 24.dp, vertical = 10.dp)
        .then(modifier),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        modifier = Modifier
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .padding(8.dp)
          .size(18.dp),
        imageVector = Icons.Outlined.Person,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Column(
        modifier = Modifier
          .padding(start = 16.dp)
          .weight(1f)
      ) {
        Text(
          modifier = Modifier.fillMaxWidth(),
          text = contributor.person_name,
          style = MaterialTheme.typography.bodyMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          modifier = Modifier.fillMaxWidth(),
          text = stringResource(contributor.role.title),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }

  @Composable
  fun MetadataRow(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onClick: () -> Unit = {}
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(horizontal = 24.dp, vertical = 10.dp)
        .then(modifier)
    ) {
      Text(
        modifier = Modifier.fillMaxWidth(),
        text = label,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Text(
        modifier = Modifier.fillMaxWidth(),
        text = value,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }

  @Composable
  fun InformationRow(
    modifier: Modifier = Modifier,
    label: String,
    value: String
  ) {
    Row(
      modifier = modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium.copy(
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      )
      Text(
        text = value,
        style = MaterialTheme.typography.bodyMedium
      )
    }
  }

  @Composable
  fun TagButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
  ) {
    Box(
      Modifier
        .clip(MaterialTheme.shapes.small)
        .background(MaterialTheme.colorScheme.surfaceVariant)
        .clickable(
          role = Role.Button,
          onClick = onClick
        )
        .then(modifier)
    ) {
      Text(
        modifier = Modifier.padding(vertical = 2.dp, horizontal = 12.dp),
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      )
    }
  }

  @Composable
  fun DeleteDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
  ) {
    if (visible) {
      AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.book_delete_title)) },
        text = { Text(pluralStringResource(R.plurals.book_delete_warning, count = 1)) },
        confirmButton = {
          TextButton(
            onClick = {
              onDelete.invoke()
              onDismiss.invoke()
            }
          ) {
            Text(stringResource(R.string.action_delete))
          }
        },
        dismissButton = {
          TextButton(onClick = onDismiss) {
            Text(stringResource(R.string.action_cancel))
          }
        }
      )
    }
  }

}
