package io.github.alessandrojean.toshokan.presentation.ui.book

import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.IconButton
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.domain.Collection
import io.github.alessandrojean.toshokan.domain.DomainBook
import io.github.alessandrojean.toshokan.domain.SearchFilters
import io.github.alessandrojean.toshokan.presentation.extensions.surfaceColorAtNavigationBarElevation
import io.github.alessandrojean.toshokan.presentation.extensions.surfaceWithTonalElevation
import io.github.alessandrojean.toshokan.presentation.ui.book.components.BookCoverFullScreenDialog
import io.github.alessandrojean.toshokan.presentation.ui.book.components.BookDeleteDialog
import io.github.alessandrojean.toshokan.presentation.ui.book.components.BookScreenContent
import io.github.alessandrojean.toshokan.presentation.ui.book.components.LinkBottomSheet
import io.github.alessandrojean.toshokan.presentation.ui.book.manage.ManageBookScreen
import io.github.alessandrojean.toshokan.presentation.ui.book.reading.ReadingScreen
import io.github.alessandrojean.toshokan.presentation.ui.core.components.EnhancedSmallTopAppBar
import io.github.alessandrojean.toshokan.presentation.ui.core.components.NoItemsFound
import io.github.alessandrojean.toshokan.presentation.ui.search.SearchScreen
import io.github.alessandrojean.toshokan.presentation.ui.theme.ModalBottomSheetExtraLargeShape
import io.github.alessandrojean.toshokan.util.extension.collectAsStateWithLifecycle
import io.github.alessandrojean.toshokan.util.extension.md5
import io.github.alessandrojean.toshokan.util.extension.push
import io.github.alessandrojean.toshokan.util.extension.toTitleParts
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.io.Serializable

data class BookScreen(val bookData: BookData) : AndroidScreen() {

  override val key = "book_screen_${bookData.identifier}"

  sealed class BookData {
    @Parcelize
    data class Database(val bookId: Long) : BookData(), Parcelable, Serializable

    @Parcelize
    data class External(val book: DomainBook) : BookData(), Parcelable, Serializable

    val identifier: Long
      get () = when (this) {
        is Database -> bookId
        is External -> book.id ?: 0L
      }
  }

  @Composable
  override fun Content() {
    val bookScreenModel = getScreenModel<BookScreenModel, BookScreenModel.Factory> { factory ->
      factory.create(bookData)
    }
    val state by bookScreenModel.state.collectAsStateWithLifecycle()
    val resultState = state as? BookScreenModel.State.Result
    val showBookNavigation by bookScreenModel.showBookNavigation.collectAsStateWithLifecycle(false)
    val navigator = LocalNavigator.currentOrThrow

    if (state == BookScreenModel.State.NotFound) {
      Scaffold(
        modifier = Modifier
          .fillMaxSize()
          .navigationBarsPadding(),
        topBar = {
          EnhancedSmallTopAppBar(
            title = {},
            contentPadding = WindowInsets.statusBars.asPaddingValues(),
            navigationIcon = {
              IconButton(onClick = { navigator.pop() }) {
                Icon(
                  painter = rememberVectorPainter(Icons.Outlined.ArrowBack),
                  contentDescription = stringResource(R.string.action_back)
                )
              }
            }
          )
        },
        content = { innerPadding ->
          NoItemsFound(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding),
            icon = Icons.Outlined.SearchOff,
            text = stringResource(R.string.error_book_not_found)
          )
        }
      )

      return
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
    val dialogNavigationColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)

    val linksBottomSheetState = rememberModalBottomSheetState(
      initialValue = ModalBottomSheetValue.Hidden,
      skipHalfExpanded = true
    )

    LaunchedEffect(resultState?.book?.coverUrl) {
      if (resultState?.book?.coverUrl.orEmpty().isBlank()) {
        palette = null
      }
    }

    // Dialog intentionally outside of the custom Material theme.
    BookDeleteDialog(
      visible = showDeleteDialog,
      onDismiss = { showDeleteDialog = false },
      onDelete = {
        bookScreenModel.delete { navigator.pop() }
      }
    )

    BackHandler(showCoverFullScreenDialog) {
      showCoverFullScreenDialog = false
    }

    BackHandler(linksBottomSheetState.isVisible) {
      scope.launch { linksBottomSheetState.hide() }
    }

    BackHandler(state is BookScreenModel.State.Writing) { }

    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      MaterialTheme(colorScheme = colorScheme) {
        val navigationBarColor = when {
          linksBottomSheetState.isVisible ->
            colorScheme.surfaceColorAtNavigationBarElevation().copy(alpha = 0.7f)
          showCoverFullScreenDialog -> dialogNavigationColor
          resultState?.neighbors == null || !showBookNavigation ->
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
              links = (state as? BookScreenModel.State.Result)?.links ?: emptyMap(),
              onLinkClick = {
                bookScreenModel.openLink(it)
                scope.launch { linksBottomSheetState.hide() }
              }
            )
          }
        ) {
          BookScreenContent(
            book = resultState?.book,
            bookContributors = resultState?.contributors.orEmpty(),
            bookTags = resultState?.tags.orEmpty(),
            bookNeighbors = resultState?.neighbors,
            inLibrary = bookScreenModel.inLibrary,
            showBookNavigation = showBookNavigation,
            showLinksButton = resultState?.links.orEmpty().isNotEmpty(),
            bottomBarTonalElevation = bottomBarTonalElevation,
            onNavigateBackClick = { navigator.pop() },
            onCoverClick = { showCoverFullScreenDialog = true },
            onLinkClick = {
              scope.launch { linksBottomSheetState.show() }
            },
            onAddToLibraryClick = {
              bookScreenModel.addToLibrary()
            },
            onShareClick = {
              bookScreenModel.shareWebUrl()
            },
            onReadingClick = {
              resultState?.book?.id?.let { bookId ->
                navigator.push(ReadingScreen(bookId))
              }
            },
            onEditClick = {
              if (resultState?.book != null) {
                navigator.push(
                  ManageBookScreen(existingBookId = resultState.book.id)
                )
              }
            },
            onDeleteClick = { showDeleteDialog = true },
            onPaginationCollectionClick = {
              navigator.push {
                val collection = Collection(
                  title = resultState?.book?.title?.toTitleParts()?.title.orEmpty(),
                  groupId = resultState?.book?.group?.id
                )
                SearchScreen(
                  filters = SearchFilters.Incomplete(
                    collections = listOfNotNull(collection.takeIf { resultState?.book != null })
                  )
                )
              }
            },
            onPaginationFirstClick = {
              bookScreenModel.navigate(resultState!!.neighbors!!.first!!.id)
            },
            onPaginationLastClick = {
              bookScreenModel.navigate(resultState!!.neighbors!!.last!!.id)
            },
            onPaginationPreviousClick = {
              bookScreenModel.navigate(resultState!!.neighbors!!.previous!!.id)
            },
            onPaginationNextClick = {
              bookScreenModel.navigate(resultState!!.neighbors!!.next!!.id)
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
        visible = showCoverFullScreenDialog && resultState?.book != null,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        BookCoverFullScreenDialog(
          book = resultState!!.book!!,
          onShareClick = { bitmap -> bookScreenModel.shareImage(bitmap, resultState.book!!) },
          onSaveClick = { bitmap -> bookScreenModel.saveImage(bitmap, resultState.book!!) },
          onEditClick = {
            showCoverFullScreenDialog = false
            
            navigator.push {
              ManageBookScreen(
                existingBookId = resultState.book!!.id,
                initialTab = ManageBookScreen.ManageBookTab.Cover
              )
            }
          },
          onDeleteClick = {
            showCoverFullScreenDialog = false
            bookScreenModel.deleteCover(resultState.book?.coverUrl)
          },
          onDismiss = { showCoverFullScreenDialog = false }
        )
      }
    }
  }

}
