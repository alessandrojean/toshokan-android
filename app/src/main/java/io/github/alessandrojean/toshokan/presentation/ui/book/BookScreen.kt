package io.github.alessandrojean.toshokan.presentation.ui.book

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.FirstPage
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LastPage
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberTopAppBarScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toBitmapOrNull
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.Book
import io.github.alessandrojean.toshokan.database.data.BookContributor
import io.github.alessandrojean.toshokan.database.data.CompleteBook
import io.github.alessandrojean.toshokan.domain.BookNeighbors
import io.github.alessandrojean.toshokan.domain.CreditRole
import io.github.alessandrojean.toshokan.presentation.extensions.surfaceColorAtNavigationBarElevation
import io.github.alessandrojean.toshokan.presentation.extensions.surfaceWithTonalElevation
import io.github.alessandrojean.toshokan.presentation.extensions.withTonalElevation
import io.github.alessandrojean.toshokan.presentation.ui.book.manage.ManageBookScreen
import io.github.alessandrojean.toshokan.presentation.ui.book.reading.ReadingScreen
import io.github.alessandrojean.toshokan.presentation.ui.core.components.ExpandedIconButton
import io.github.alessandrojean.toshokan.presentation.ui.theme.DividerOpacity
import io.github.alessandrojean.toshokan.presentation.ui.theme.ModalBottomSheetShape
import io.github.alessandrojean.toshokan.util.extension.collectAsStateWithLifecycle
import io.github.alessandrojean.toshokan.util.extension.formatToLocaleDate
import io.github.alessandrojean.toshokan.util.extension.toLanguageDisplayName
import io.github.alessandrojean.toshokan.util.extension.toLocaleCurrencyString
import io.github.alessandrojean.toshokan.util.extension.toLocaleString
import io.github.alessandrojean.toshokan.util.toIsbnInformation
import kotlinx.coroutines.launch
import java.lang.Float.min
import java.text.DateFormat
import kotlin.math.ceil

data class BookScreen(val bookId: Long) : AndroidScreen() {

  @Composable
  override fun Content() {
    val bookScreenModel = getScreenModel<BookScreenModel, BookScreenModel.Factory> { factory ->
      factory.create(bookId)
    }
    val book by bookScreenModel.book.collectAsStateWithLifecycle(null)
    val bookContributors by bookScreenModel.contributors.collectAsStateWithLifecycle(emptyList())
    val bookNeighbors by bookScreenModel.findSeriesVolumes(book).collectAsStateWithLifecycle(null)
    val navigator = LocalNavigator.currentOrThrow

    val scrollState = rememberScrollState()
    val topAppBarScrollState = rememberTopAppBarScrollState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior(topAppBarScrollState) }

    val defaultColorScheme = MaterialTheme.colorScheme
    val isSystemInDarkTheme = isSystemInDarkTheme()

    val colorScheme = remember(bookScreenModel.palette, isSystemInDarkTheme) {
      when {
        bookScreenModel.palette?.vibrantSwatch?.rgb == null -> defaultColorScheme
        isSystemInDarkTheme -> darkColorScheme(
          primary = Color(bookScreenModel.palette!!.vibrantSwatch!!.rgb)
        )
        else -> lightColorScheme(
          primary = Color(bookScreenModel.palette!!.vibrantSwatch!!.rgb)
        )
      }
    }

    LaunchedEffect(book?.cover_url) {
      if (book?.cover_url.orEmpty().isBlank()) {
        bookScreenModel.resetPalette()
      }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    // Dialog intentionally outside of the custom Material theme.
    DeleteDialog(
      visible = showDeleteDialog,
      onDismiss = { showDeleteDialog = false },
      onDelete = {
        bookScreenModel.delete()
        navigator.pop()
      }
    )

    MaterialTheme(colorScheme = colorScheme) {
      val bottomBarTonalElevation = 24.dp

      val systemBarColor = Color.Transparent
      val systemBarDarkIcons = !isSystemInDarkTheme
      val navigationBarColor = if (bookNeighbors == null) {
        colorScheme.surfaceColorAtNavigationBarElevation().copy(alpha = 0.7f)
      } else {
        colorScheme.surfaceWithTonalElevation(bottomBarTonalElevation)
      }
      val systemUiController = rememberSystemUiController()
      val localConfiguration = LocalConfiguration.current
      val localDensity = LocalDensity.current

      val scrolledTopBarContainerColor = colorScheme.surfaceWithTonalElevation(6.dp)
      val maxPoint = remember(localConfiguration, localDensity) {
        with(localDensity) {
          localConfiguration.screenWidthDp.dp.toPx() * 0.8f
        }
      }
      val currentScrollBounded = min(scrollState.value.toFloat(), maxPoint)
      val scrollPercentage = currentScrollBounded / maxPoint
      val coverBottomOffsetDp = 18f

      val topBarContainerColor by remember(scrollPercentage, scrolledTopBarContainerColor) {
        derivedStateOf {
          scrolledTopBarContainerColor.copy(alpha = scrollPercentage)
        }
      }

      SideEffect {
        systemUiController.setStatusBarColor(
          color = systemBarColor,
          darkIcons = systemBarDarkIcons
        )

        if (navigator.lastItem is BookScreen) {
          systemUiController.setNavigationBarColor(
            color = navigationBarColor
          )
        }
      }

      Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceWithTonalElevation(6.dp),
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
                    modifier = Modifier.graphicsLayer(alpha = scrollPercentage),
                    text = book?.title.orEmpty(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                },
                actions = {
                  IconButton(
                    enabled = book != null,
                    onClick = { bookScreenModel.toggleFavorite() }
                  ) {
                    Icon(
                      imageVector = if (book?.is_favorite == true) {
                        Icons.Filled.Star
                      } else {
                        Icons.Outlined.StarOutline
                      },
                      contentDescription = if (book?.is_favorite == true) {
                        stringResource(R.string.action_remove_from_favorites)
                      } else {
                        stringResource(R.string.action_add_to_favorites)
                      }
                    )
                  }
                  IconButton(onClick = { }) {
                    Icon(
                      imageVector = Icons.Outlined.Link,
                      contentDescription = null
                    )
                  }
                  IconButton(onClick = { }) {
                    Icon(
                      imageVector = Icons.Outlined.MoreVert,
                      contentDescription = null
                    )
                  }
                }
              )
              Divider(
                modifier = Modifier
                  .graphicsLayer(alpha = scrollPercentage),
                color = LocalContentColor.current.copy(alpha = DividerOpacity)
              )
            }
          }
        },
        content = { innerPadding ->
          Column(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(scrollState)
          ) {
            BookCoverBox(
              modifier = Modifier
                .fillMaxWidth()
                .offset(y = (ceil(100f * scrollPercentage)).dp)
                .graphicsLayer(alpha = 0.4f + 0.6f * (1f - scrollPercentage)),
              coverUrl = book?.cover_url.orEmpty(),
              contentDescription = book?.title,
              bottomOffsetDp = coverBottomOffsetDp,
              topBarHeightDp = 52f,
              onSuccess = { bitmap -> bookScreenModel.findPalette(bitmap) },
            )
            BookInformation(
              modifier = Modifier
                .offset(y = (-coverBottomOffsetDp).dp)
                .fillMaxWidth(),
              bottomPadding = if (bookNeighbors != null) {
                (innerPadding.calculateBottomPadding() -
                  WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                  .coerceAtLeast(0.dp)
              } else {
                0.dp
              },
              book = book,
              contributors = bookContributors,
              onReadingClick = { navigator.push(ReadingScreen(bookId)) },
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
        },
        bottomBar = {
          BottomPagination(
            modifier = Modifier.navigationBarsPadding(),
            tonalElevation = bottomBarTonalElevation,
            bookNeighbors = bookNeighbors,
            onFirstClick = {
              navigator.replace(BookScreen(bookNeighbors!!.first!!.id))
            },
            onLastClick = {
              navigator.replace(BookScreen(bookNeighbors!!.last!!.id))
            },
            onPreviousClick = {
              navigator.replace(BookScreen(bookNeighbors!!.previous!!.id))
            },
            onNextClick = {
              navigator.replace(BookScreen(bookNeighbors!!.next!!.id))
            }
          )
        }
      )
    }
  }

  @Composable
  fun BottomPagination(
    modifier: Modifier = Modifier,
    tonalElevation: Dp = 12.dp,
    bookNeighbors: BookNeighbors?,
    onFirstClick: () -> Unit,
    onLastClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
  ) {
    AnimatedVisibility(
      visible = bookNeighbors != null,
      enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
      exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
    ) {
      Column(
        modifier = Modifier
          .background(MaterialTheme.colorScheme.surfaceWithTonalElevation(tonalElevation))
          .then(modifier)
      ) {
        Divider(color = LocalContentColor.current.copy(alpha = DividerOpacity))
        BottomAppBar(tonalElevation = tonalElevation) {
          Spacer(modifier = Modifier.weight(1f))
          IconButton(
            onClick = onFirstClick,
            enabled = bookNeighbors?.first != null &&
              bookNeighbors.first.id != bookNeighbors.current?.id
          ) {
            Icon(
              imageVector = Icons.Outlined.FirstPage,
              contentDescription = stringResource(R.string.action_first)
            )
          }
          IconButton(
            onClick = onPreviousClick,
            enabled = bookNeighbors?.previous != null
          ) {
            Icon(
              imageVector = Icons.Outlined.ChevronLeft,
              contentDescription = stringResource(R.string.action_previous)
            )
          }
          IconButton(
            onClick = onNextClick,
            enabled = bookNeighbors?.next != null
          ) {
            Icon(
              imageVector = Icons.Outlined.ChevronRight,
              contentDescription = stringResource(R.string.action_next)
            )
          }
          IconButton(
            onClick = onLastClick,
            enabled = bookNeighbors?.last != null &&
              bookNeighbors.last.id != bookNeighbors.current?.id
          ) {
            Icon(
              imageVector = Icons.Outlined.LastPage,
              contentDescription = stringResource(R.string.action_last)
            )
          }
        }
      }
    }
  }

  @Composable
  fun BookCoverBox(
    modifier: Modifier = Modifier,
    coverUrl: String,
    contentDescription: String?,
    containerColor: Color = MaterialTheme.colorScheme.background,
    topBarHeightDp: Float = 64f,
    bottomOffsetDp: Float = 18f,
    onSuccess: (Bitmap?) -> Unit
  ) {
    val verticalPadding = (topBarHeightDp + bottomOffsetDp).dp +
      WindowInsets.statusBars
        .asPaddingValues()
        .calculateTopPadding()

    var hasError by remember { mutableStateOf(false) }

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f / 1.15f)
        .background(containerColor)
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
          .blur(4.dp)
          .graphicsLayer(alpha = 0.15f),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
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
        if (hasError || coverUrl.isBlank()) {
          Icon(
            modifier = Modifier.size(96.dp),
            imageVector = Icons.Outlined.Image,
            contentDescription = null,
            tint = LocalContentColor.current.copy(alpha = 0.15f)
          )
        }

        AsyncImage(
          model = ImageRequest.Builder(LocalContext.current)
            .data(coverUrl)
            .crossfade(true)
            .allowHardware(false)
            .build(),
          modifier = Modifier.clip(MaterialTheme.shapes.large),
          contentDescription = contentDescription,
          contentScale = ContentScale.Inside,
          onSuccess = { state ->
            onSuccess.invoke(state.result.drawable.toBitmapOrNull())
            hasError = false
          },
          onError = { hasError = true }
        )
      }
    }
  }

  @Composable
  fun BookInformation(
    modifier: Modifier = Modifier,
    book: CompleteBook?,
    contributors: List<BookContributor>,
    color: Color = MaterialTheme.colorScheme.surface,
    tonalElevation: Dp = 6.dp,
    bottomPadding: Dp = 0.dp,
    onReadingClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
  ) {
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

    Surface(
      modifier = modifier,
      shape = ModalBottomSheetShape,
      color = color,
      tonalElevation = tonalElevation
    ) {
      Column(
        modifier = Modifier
          .padding(
            top = 24.dp,
            bottom = bottomPadding
          )
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
            InformationRow(
              label = stringResource(R.string.code),
              value = book?.code.orEmpty()
            )
          }
          InformationRow(
            label = stringResource(R.string.created_at),
            value = book?.created_at?.formatToLocaleDate() ?: ""
          )
          InformationRow(
            label = stringResource(R.string.updated_at),
            value = book?.updated_at?.formatToLocaleDate() ?: ""
          )
        }
        Text(
          modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 12.dp),
          text = stringResource(R.string.contributors),
          style = MaterialTheme.typography.titleLarge
        )
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
        ) {
          visibleContributors.forEach { contributor ->
            ContributorRow(
              contributor = contributor,
              onClick = { /* TODO */ }
            )
          }
          if (contributorsToggleable) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(contributorsButtonHeight.dp)
            ) {
              if (!contributorsExpanded) {
                ContributorRow(contributor = contributors[minContributors])
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
          modifier = Modifier.padding(
            start = 24.dp,
            end = 24.dp,
            top = 32.dp,
            bottom = 12.dp
          ),
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
        if (book?.latest_reading != null) {
          MetadataRow(
            label = stringResource(R.string.latest_reading),
            value = book.latest_reading.formatToLocaleDate(format = DateFormat.LONG),
            onClick = onReadingClick
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

  }

  @Composable
  fun ContributorRow(
    modifier: Modifier = Modifier,
    contributor: BookContributor,
    onClick: (() -> Unit)? = null
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clickable(
          enabled = onClick != null,
          onClick = onClick ?: {}
        )
        .padding(horizontal = 24.dp, vertical = 10.dp)
        .then(modifier),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        modifier = Modifier
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.surfaceVariant.withTonalElevation(6.dp))
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
