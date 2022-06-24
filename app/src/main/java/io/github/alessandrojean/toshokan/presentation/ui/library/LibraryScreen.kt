package io.github.alessandrojean.toshokan.presentation.ui.library

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.paging.compose.collectAsLazyPagingItems
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.extensions.surfaceWithTonalElevation
import io.github.alessandrojean.toshokan.presentation.ui.barcodescanner.BarcodeScannerScreen
import io.github.alessandrojean.toshokan.presentation.ui.book.BookScreen
import io.github.alessandrojean.toshokan.presentation.ui.book.components.BookDeleteDialog
import io.github.alessandrojean.toshokan.presentation.ui.book.manage.ManageBookScreen
import io.github.alessandrojean.toshokan.presentation.ui.core.components.EnhancedSmallTopAppBar
import io.github.alessandrojean.toshokan.presentation.ui.core.components.NoItemsFound
import io.github.alessandrojean.toshokan.presentation.ui.core.components.SelectionTopAppBar
import io.github.alessandrojean.toshokan.presentation.ui.core.provider.LocalNavigationBarControl
import io.github.alessandrojean.toshokan.presentation.ui.isbnlookup.IsbnLookupScreen
import io.github.alessandrojean.toshokan.presentation.ui.library.components.LibraryGrid
import io.github.alessandrojean.toshokan.presentation.ui.search.SearchScreen
import io.github.alessandrojean.toshokan.util.ConnectionState
import io.github.alessandrojean.toshokan.util.connectivityState
import io.github.alessandrojean.toshokan.util.extension.collectAsStateWithLifecycle
import io.github.alessandrojean.toshokan.util.extension.deviceHasCamera
import io.github.alessandrojean.toshokan.util.extension.plus
import io.github.alessandrojean.toshokan.util.extension.push
import kotlinx.coroutines.launch

class LibraryScreen : AndroidScreen() {

  override val key = "library_screen"

  @Composable
  override fun Content() {
    var showCreateBookSheet by remember { mutableStateOf(false) }
    val navigator = LocalNavigator.currentOrThrow
    val scope = rememberCoroutineScope()

    val screenModel = getScreenModel<LibraryScreenModel>()
    val state by screenModel.state.collectAsStateWithLifecycle()
    val topAppBarScrollState = rememberTopAppBarScrollState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior(topAppBarScrollState) }

    val pagerState = rememberPagerState(initialPage = 0)

    val selectionMode = remember(screenModel.selection.size) {
      screenModel.selection.isNotEmpty()
    }

    val navigationBarControl = LocalNavigationBarControl.current

    LaunchedEffect(selectionMode) {
      if (selectionMode) {
        navigationBarControl.hide()
      } else {
        navigationBarControl.show()
      }
    }

    LaunchedEffect(navigator.lastItem) {
      if (navigator.lastItem !is LibraryScreen) {
        screenModel.selection.clear()
      }
    }

    BackHandler(selectionMode) {
      screenModel.selection.clear()
    }

    val topAppBarBackgroundColors = TopAppBarDefaults.smallTopAppBarColors(
      containerColor = if (selectionMode) {
        MaterialTheme.colorScheme.surfaceVariant
      } else {
        MaterialTheme.colorScheme.surface
      },
      scrolledContainerColor = if (selectionMode){
        MaterialTheme.colorScheme.surfaceVariant
      } else {
        MaterialTheme.colorScheme.surfaceWithTonalElevation(6.dp)
      }
    )

    if (showCreateBookSheet) {
      CreateBookSheet(
        onScanBarcodeClick = { navigator.push(BarcodeScannerScreen()) },
        onIsbnSearchClick = { navigator.push(IsbnLookupScreen()) },
        onFillManuallyClick = { navigator.push(ManageBookScreen()) },
        onDismiss = { showCreateBookSheet = false }
      )
    }

    val internetConnection by connectivityState()

    val scrollableTabs: @Composable ColumnScope.() -> Unit = @Composable {
      if (state is LibraryScreenModel.State.Library) {
        // TODO: Fix the tab row width when having a few items.
        ScrollableTabRow(
          modifier = Modifier.fillMaxWidth(),
          selectedTabIndex = pagerState.currentPage,
          edgePadding = 12.dp,
          containerColor = Color.Transparent,
          indicator = { tabPositions ->
            TabRowDefaults.Indicator(
              Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage])
            )
          },
        ) {
          (state as LibraryScreenModel.State.Library).tabs.forEachIndexed { index, tab ->
            Tab(
              text = { Text(tab.group.name) },
              selected = pagerState.currentPage == index,
              selectedContentColor = MaterialTheme.colorScheme.primary,
              unselectedContentColor = MaterialTheme.colorScheme.onBackground,
              onClick = {
                scope.launch { pagerState.animateScrollToPage(index) }
              }
            )
          }
        }
      }
    }

    var showDeleteWarning by rememberSaveable { mutableStateOf(false) }

    BookDeleteDialog(
      visible = showDeleteWarning,
      isMultiple = screenModel.selection.size > 1,
      onDismiss = { showDeleteWarning = false },
      onDelete = {
        screenModel.deleteBooks(screenModel.selection.toList())
        screenModel.selection.clear()
      }
    )

    Scaffold(
      modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
      topBar = {
        Crossfade(targetState = selectionMode) { selectionMode ->
          if (selectionMode) {
            SelectionTopAppBar(
              colors = topAppBarBackgroundColors,
              selectionCount = screenModel.selection.size,
              onClearSelectionClick = { screenModel.selection.clear() },
              onEditClick = {
                navigator.push {
                  ManageBookScreen(
                    existingBookId = screenModel.selection.first()
                  )
                }

                screenModel.selection.clear()
              },
              onDeleteClick = { showDeleteWarning = true },
              scrollBehavior = scrollBehavior,
              content = scrollableTabs
            )
          } else {
            EnhancedSmallTopAppBar(
              colors = topAppBarBackgroundColors,
              contentPadding = WindowInsets.statusBars.asPaddingValues(),
              scrollBehavior = scrollBehavior,
              title = { Text(stringResource(R.string.library)) },
              actions = {
                IconButton(onClick = { navigator.push(SearchScreen()) }) {
                  Icon(
                    Icons.Default.Search,
                    contentDescription = stringResource(R.string.action_search)
                  )
                }
              },
              content = scrollableTabs
            )
          }
        }
      },
      content = { innerPadding ->
        Crossfade(
          targetState = state,
          modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
        ) { state ->
          when (state) {
            LibraryScreenModel.State.Empty -> {
              NoItemsFound(
                modifier = Modifier.fillMaxSize(),
                icon = Icons.Outlined.Book
              )
            }
            is LibraryScreenModel.State.Library -> {
              HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = pagerState,
                count = state.tabs.size,
                verticalAlignment = Alignment.Top,
              ) { page ->
                LibraryGrid(
                  modifier = Modifier.fillMaxSize(),
                  contentPadding = PaddingValues(4.dp) +
                    if (selectionMode) WindowInsets.navigationBars.asPaddingValues()
                    else PaddingValues(),
                  books = state.tabs[page].books.collectAsLazyPagingItems(),
                  selection = screenModel.selection,
                  onBookClick = { book ->
                    if (selectionMode) {
                      screenModel.toggleSelected(book.id)
                    } else {
                      navigator.push(BookScreen(book.id))
                    }
                  },
                  onBookLongClick = { book ->
                    screenModel.toggleSelected(book.id)
                  }
                )
              }
            }
          }
        }
      },
      floatingActionButtonPosition = FabPosition.End,
      floatingActionButton = {
        AnimatedVisibility(
          visible = !selectionMode,
          enter = fadeIn(),
          exit = fadeOut()
        ) {
          FloatingActionButton(
            onClick = {
              if (internetConnection is ConnectionState.Unavailable) {
                navigator.push(ManageBookScreen())
              } else {
                showCreateBookSheet = true
              }
            }
          ) {
            Icon(
              Icons.Default.Add,
              contentDescription = stringResource(R.string.action_new_book)
            )
          }
        }
      }
    )
  }

  @Composable
  fun CreateBookSheet(
    onScanBarcodeClick: () -> Unit,
    onIsbnSearchClick: () -> Unit,
    onFillManuallyClick: () -> Unit,
    onDismiss: () -> Unit
  ) {
    val context = LocalContext.current
    val hasCamera = remember { context.deviceHasCamera }

    Dialog(onDismissRequest = onDismiss) {
      Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 6.dp,
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 18.dp)
        ) {
          Text(
            modifier = Modifier
              .fillMaxWidth()
              .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
            maxLines = 1,
            text = stringResource(R.string.create_book),
            color =  MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall
          )
          if (hasCamera) {
            CreateBookSheetItem(
              icon = {
                Icon(
                  painter = painterResource(R.drawable.ic_barcode_scanner_outlined),
                  contentDescription = stringResource(R.string.action_scan_barcode),
                  tint = MaterialTheme.colorScheme.onSurface
                )
              },
              text = stringResource(R.string.action_scan_barcode),
              onClick = {
                onScanBarcodeClick()
                onDismiss()
              }
            )
          }
          CreateBookSheetItem(
            icon = Icons.Outlined.Search,
            text = stringResource(R.string.action_search_by_isbn),
            onClick = {
              onIsbnSearchClick()
              onDismiss()
            }
          )
          CreateBookSheetItem(
            icon = Icons.Outlined.EditNote,
            text = stringResource(R.string.action_fill_manually),
            onClick = {
              onFillManuallyClick()
              onDismiss()
            }
          )
        }
      }
    }
  }

  @Composable
  fun CreateBookSheetItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
  ) {
    CreateBookSheetItem(
      icon = {
        Icon(
          imageVector = icon,
          contentDescription = text,
          tint = MaterialTheme.colorScheme.onSurface
        )
      },
      text = text,
      onClick = onClick
    )
  }

  @Composable
  fun CreateBookSheetItem(
    icon: @Composable () -> Unit,
    text: String,
    onClick: () -> Unit
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(vertical = 16.dp, horizontal = 24.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
      icon.invoke()
      Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }

}
