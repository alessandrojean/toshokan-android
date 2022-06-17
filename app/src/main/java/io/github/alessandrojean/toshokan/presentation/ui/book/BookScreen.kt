package io.github.alessandrojean.toshokan.presentation.ui.book

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.github.alessandrojean.toshokan.domain.SearchFilters
import io.github.alessandrojean.toshokan.presentation.extensions.surfaceColorAtNavigationBarElevation
import io.github.alessandrojean.toshokan.presentation.extensions.surfaceWithTonalElevation
import io.github.alessandrojean.toshokan.presentation.ui.book.components.BookCoverFullScreenDialog
import io.github.alessandrojean.toshokan.presentation.ui.book.components.BookDeleteDialog
import io.github.alessandrojean.toshokan.presentation.ui.book.components.BookScreenContent
import io.github.alessandrojean.toshokan.presentation.ui.book.components.LinkBottomSheet
import io.github.alessandrojean.toshokan.presentation.ui.book.manage.ManageBookScreen
import io.github.alessandrojean.toshokan.presentation.ui.book.reading.ReadingScreen
import io.github.alessandrojean.toshokan.presentation.ui.search.SearchScreen
import io.github.alessandrojean.toshokan.presentation.ui.theme.ModalBottomSheetExtraLargeShape
import io.github.alessandrojean.toshokan.util.extension.collectAsStateWithLifecycle
import io.github.alessandrojean.toshokan.util.extension.push
import io.github.alessandrojean.toshokan.util.extension.toTitleParts
import kotlinx.coroutines.launch

data class BookScreen(val bookId: Long) : AndroidScreen() {

  @Composable
  override fun Content() {
    val bookScreenModel = getScreenModel<BookScreenModel, BookScreenModel.Factory> { factory ->
      factory.create(bookId)
    }
    val book by bookScreenModel.book.collectAsStateWithLifecycle(null)
    val simpleBook by bookScreenModel.simpleBook.collectAsStateWithLifecycle(null)
    val bookContributors by bookScreenModel.contributors.collectAsStateWithLifecycle(emptyList())
    val bookNeighbors by bookScreenModel.findSeriesVolumes(book).collectAsStateWithLifecycle(null)
    val showBookNavigation by bookScreenModel.showBookNavigation.collectAsStateWithLifecycle(false)
    val navigator = LocalNavigator.currentOrThrow
    val context = LocalContext.current

    val bookLinks = remember(book) {
      bookScreenModel.findBookLinks(book)
        .sortedBy { context.getString(it.name) }
        .groupBy { it.category }
    }

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val systemUiController = rememberSystemUiController()
    val scope = rememberCoroutineScope()

    var palette by remember { mutableStateOf<Palette?>(null) }

    val defaultColorScheme = MaterialTheme.colorScheme
    val coverDominantColor = palette?.vibrantSwatch?.rgb
    val colorScheme = remember(coverDominantColor, isSystemInDarkTheme) {
      when {
        coverDominantColor == null -> defaultColorScheme
        isSystemInDarkTheme -> darkColorScheme(
          primary = Color(coverDominantColor)
        )
        else -> lightColorScheme(
          primary = Color(coverDominantColor)
        )
      }
    }

    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showCoverFullScreenDialog by rememberSaveable { mutableStateOf(false) }

    val bottomBarTonalElevation = 24.dp
    val dialogNavigationColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)

    val linksBottomSheetState = rememberModalBottomSheetState(
      initialValue = ModalBottomSheetValue.Hidden,
      skipHalfExpanded = true
    )

    LaunchedEffect(book?.cover_url) {
      if (book?.cover_url.orEmpty().isBlank()) {
        palette = null
      }
    }

    // Dialog intentionally outside of the custom Material theme.
    BookDeleteDialog(
      visible = showDeleteDialog,
      onDismiss = { showDeleteDialog = false },
      onDelete = {
        bookScreenModel.delete()
        navigator.pop()
      }
    )

    BackHandler(showCoverFullScreenDialog) {
      showCoverFullScreenDialog = false
    }

    BackHandler(linksBottomSheetState.isVisible) {
      scope.launch { linksBottomSheetState.hide() }
    }

    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      MaterialTheme(colorScheme = colorScheme) {
        val navigationBarColor = when {
          linksBottomSheetState.isVisible ->
            colorScheme.surfaceColorAtNavigationBarElevation().copy(alpha = 0.7f)
          showCoverFullScreenDialog -> dialogNavigationColor
          bookNeighbors == null || !showBookNavigation ->
            colorScheme.surfaceColorAtNavigationBarElevation().copy(alpha = 0.7f)
          else -> colorScheme.surfaceWithTonalElevation(bottomBarTonalElevation)
        }

        SideEffect {
          if (navigator.lastItem is BookScreen) {
            systemUiController.setNavigationBarColor(
              color = navigationBarColor
            )
          }
        }

        // TODO: Replace with Material3 when available.
        ModalBottomSheetLayout(
          sheetState = linksBottomSheetState,
          sheetShape = ModalBottomSheetExtraLargeShape,
          sheetBackgroundColor = Color.Transparent,
          scrimColor = colorScheme.surface.copy(alpha = 0.9f),
          sheetContent = {
            LinkBottomSheet(
              links = bookLinks,
              onLinkClick = {
                bookScreenModel.openLink(it)
                scope.launch { linksBottomSheetState.hide() }
              }
            )
          }
        ) {
          BookScreenContent(
            book = book,
            simpleBook = simpleBook,
            bookContributors = bookContributors,
            bookNeighbors = bookNeighbors,
            showBookNavigation = showBookNavigation,
            showLinksButton = bookLinks.isNotEmpty(),
            bottomBarTonalElevation = bottomBarTonalElevation,
            onNavigateBackClick = { navigator.pop() },
            onCoverClick = { showCoverFullScreenDialog = true },
            onLinkClick = {
              scope.launch { linksBottomSheetState.show() }
            },
            onReadingClick = { navigator.push(ReadingScreen(bookId)) },
            onEditClick = {
              if (book != null) {
                navigator.push(
                  ManageBookScreen(existingBookId = book!!.id)
                )
              }
            },
            onDeleteClick = { showDeleteDialog = true },
            onPaginationCollectionClick = {
              val searchFilters = SearchFilters.Incomplete(
                collections = listOf(book!!.title.toTitleParts().title)
              )
              navigator.push(SearchScreen(searchFilters))
            },
            onPaginationFirstClick = {
              navigator.replace(BookScreen(bookNeighbors!!.first!!.id))
            },
            onPaginationLastClick = {
              navigator.replace(BookScreen(bookNeighbors!!.last!!.id))
            },
            onPaginationPreviousClick = {
              navigator.replace(BookScreen(bookNeighbors!!.previous!!.id))
            },
            onPaginationNextClick = {
              navigator.replace(BookScreen(bookNeighbors!!.next!!.id))
            },
            onFavoriteChange = { bookScreenModel.toggleFavorite() },
            onImageSuccess = { drawableBitmap ->
              drawableBitmap?.let {
                palette = Palette.Builder(it).generate()
              }
            }
          )
        }
      }

      AnimatedVisibility(
        modifier = Modifier.fillMaxSize(),
        visible = showCoverFullScreenDialog && book != null,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        BookCoverFullScreenDialog(
          book = simpleBook,
          onShareClick = { bitmap -> bookScreenModel.shareImage(bitmap, book!!) },
          onSaveClick = { bitmap -> bookScreenModel.saveImage(bitmap, book!!) },
          onEditClick = {
            showCoverFullScreenDialog = false
            
            navigator.push {
              ManageBookScreen(
                existingBookId = book!!.id,
                initialTab = ManageBookScreen.ManageBookTab.Cover
              )
            }
          },
          onDismiss = { showCoverFullScreenDialog = false }
        )
      }
    }
  }

}
