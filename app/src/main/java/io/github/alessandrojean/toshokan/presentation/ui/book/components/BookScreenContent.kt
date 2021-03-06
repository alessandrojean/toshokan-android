package io.github.alessandrojean.toshokan.presentation.ui.book.components

import android.graphics.Bitmap
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.Book
import io.github.alessandrojean.toshokan.database.data.BookContributor
import io.github.alessandrojean.toshokan.database.data.CompleteBook
import io.github.alessandrojean.toshokan.database.data.Tag
import io.github.alessandrojean.toshokan.domain.BookNeighbors
import io.github.alessandrojean.toshokan.domain.DomainBook
import io.github.alessandrojean.toshokan.domain.DomainContributor
import io.github.alessandrojean.toshokan.domain.DomainRelation
import io.github.alessandrojean.toshokan.domain.DomainTag
import io.github.alessandrojean.toshokan.presentation.extensions.surfaceWithTonalElevation
import io.github.alessandrojean.toshokan.presentation.ui.theme.DividerOpacity
import io.github.alessandrojean.toshokan.util.extension.bottom
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.min

@Composable
fun BookScreenContent(
  modifier: Modifier = Modifier,
  book: DomainBook?,
  bookContributors: List<DomainContributor>,
  bookTags: List<DomainTag>,
  bookNeighbors: BookNeighbors?,
  inLibrary: Boolean,
  showBookNavigation: Boolean = true,
  showLinksButton: Boolean = true,
  bottomBarTonalElevation: Dp = 24.dp,
  onNavigateBackClick: () -> Unit,
  onAddToLibraryClick: () -> Unit,
  onShareClick: () -> Unit,
  onReadingClick: () -> Unit,
  onEditClick: () -> Unit,
  onDeleteClick: () -> Unit,
  onPaginationCollectionClick: () -> Unit,
  onPaginationFirstClick: () -> Unit,
  onPaginationLastClick: () -> Unit,
  onPaginationPreviousClick: () -> Unit,
  onPaginationNextClick: () -> Unit,
  onCoverClick: () -> Unit,
  onLinkClick: () -> Unit,
  onFavoriteChange: (Boolean) -> Unit,
  onImageSuccess: (Bitmap?) -> Unit
) {
  val scrollState = rememberScrollState()
  val topAppBarScrollState = rememberTopAppBarScrollState()
  val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior(topAppBarScrollState) }

  val localConfiguration = LocalConfiguration.current
  val localDensity = LocalDensity.current

  val scrolledTopBarContainerColor = MaterialTheme.colorScheme.surfaceWithTonalElevation(6.dp)
  val maxPoint = remember(localConfiguration, localDensity) {
    with(localDensity) {
      localConfiguration.screenWidthDp.dp.toPx() * 0.8f
    }
  }

  val currentScrollBounded by remember {
    derivedStateOf { min(scrollState.value.toFloat(), maxPoint) }
  }
  val scrollPercentage by remember {
    derivedStateOf { (currentScrollBounded / maxPoint).coerceIn(0f, 1f) }
  }
  val coverBottomOffsetDp = 18f

  val topBarContainerColor by remember(scrolledTopBarContainerColor) {
    derivedStateOf { scrolledTopBarContainerColor.copy(alpha = scrollPercentage) }
  }

  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  val context = LocalContext.current

  val navigationBarsPadding = if (bookNeighbors == null) {
    WindowInsets.navigationBars.asPaddingValues()
  } else {
    PaddingValues()
  }

  Scaffold(
    modifier = Modifier
      .nestedScroll(scrollBehavior.nestedScrollConnection)
      .then(modifier),
    containerColor = MaterialTheme.colorScheme.surfaceWithTonalElevation(6.dp),
    snackbarHost = {
      SnackbarHost(
        modifier = Modifier.padding(navigationBarsPadding),
        hostState = snackbarHostState
      )
    },
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
              IconButton(onClick = onNavigateBackClick) {
                Icon(
                  imageVector = Icons.Outlined.ArrowBack,
                  contentDescription = stringResource(R.string.action_back)
                )
              }
            },
            title = {
              Text(
                modifier = Modifier.graphicsLayer { alpha = scrollPercentage },
                text = book?.title.orEmpty(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            },
            actions = {
              IconToggleButton(
                checked = book?.isFavorite == true,
                onCheckedChange = { newValue ->
                  onFavoriteChange(newValue)
                  scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(
                      message = if (newValue) {
                        context.getString(R.string.book_added_to_favorites)
                      } else {
                        context.getString(R.string.book_removed_from_favorites)
                      }
                    )
                  }
                },
                enabled = book?.id != null,
                colors = IconButtonDefaults.iconToggleButtonColors(
                  contentColor = LocalContentColor.current,
                  checkedContentColor = LocalContentColor.current
                )
              ) {
                Icon(
                  imageVector = if (book?.isFavorite == true) {
                    Icons.Filled.Star
                  } else {
                    Icons.Outlined.StarOutline
                  },
                  contentDescription = if (book?.isFavorite == true) {
                    stringResource(R.string.action_remove_from_favorites)
                  } else {
                    stringResource(R.string.action_add_to_favorites)
                  }
                )
              }

              if (showLinksButton) {
                IconButton(onClick = onLinkClick) {
                  Icon(
                    imageVector = Icons.Outlined.Link,
                    contentDescription = stringResource(R.string.action_view_links)
                  )
                }
              }

              if (book != null) {
                IconButton(onClick = onShareClick) {
                  Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = stringResource(R.string.action_share)
                  )
                }
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
        val coverOffset by animateDpAsState(
          targetValue = ceil(100f * scrollPercentage).dp
        )
        val coverOpacity by animateFloatAsState(0.4f + 0.6f * (1.0f - scrollPercentage))

        BookCoverBox(
          modifier = Modifier
            .fillMaxWidth()
            .offset(y = coverOffset)
            .graphicsLayer {
              alpha = coverOpacity
            },
          book = book,
          bottomOffsetDp = coverBottomOffsetDp,
          topBarHeightDp = 52f,
          onImageSuccess = onImageSuccess,
          onCoverClick = onCoverClick,
        )
        BookInformation(
          modifier = Modifier
            .offset(y = (-coverBottomOffsetDp).dp)
            .fillMaxWidth(),
          bottomPadding = innerPadding.bottom,
          bottomBarVisible = bookNeighbors != null && showBookNavigation,
          book = book,
          contributors = bookContributors,
          tags = bookTags,
          hasBookNeighbors = bookNeighbors != null,
          inLibrary = inLibrary,
          onAddToLibraryClick = onAddToLibraryClick,
          onReadingClick = onReadingClick,
          onEditClick = onEditClick,
          onDeleteClick = onDeleteClick
        )
      }
    },
    bottomBar = {
      BookCollectionBottomPagination(
        modifier = Modifier.navigationBarsPadding(),
        tonalElevation = bottomBarTonalElevation,
        visible = showBookNavigation,
        enabled = book != null,
        bookNeighbors = bookNeighbors,
        onCollectionClick = onPaginationCollectionClick,
        onFirstClick = {
          onPaginationFirstClick()
          scope.launch { scrollState.animateScrollTo(0) }
        },
        onLastClick = {
          onPaginationLastClick()
          scope.launch { scrollState.animateScrollTo(0) }
        },
        onPreviousClick = {
          onPaginationPreviousClick()
          scope.launch { scrollState.animateScrollTo(0) }
        },
        onNextClick = {
          onPaginationNextClick()
          scope.launch { scrollState.animateScrollTo(0) }
        }
      )
    }
  )
}
