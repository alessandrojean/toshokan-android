package io.github.alessandrojean.toshokan.presentation.ui.library

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.domain.Library
import io.github.alessandrojean.toshokan.presentation.ui.barcodescanner.BarcodeScannerScreen
import io.github.alessandrojean.toshokan.presentation.ui.book.BookScreen
import io.github.alessandrojean.toshokan.presentation.ui.book.manage.ManageBookScreen
import io.github.alessandrojean.toshokan.presentation.ui.core.components.NoItemsFound
import io.github.alessandrojean.toshokan.presentation.ui.isbnlookup.IsbnLookupScreen
import io.github.alessandrojean.toshokan.presentation.ui.library.components.LibraryGrid
import io.github.alessandrojean.toshokan.util.extension.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

class LibraryScreen : AndroidScreen() {

  @Composable
  override fun Content() {
    var showCreateBookSheet by remember { mutableStateOf(false) }
    val navigator = LocalNavigator.currentOrThrow
    val scope = rememberCoroutineScope()

    val libraryViewModel = getViewModel<LibraryViewModel>()
    val library by libraryViewModel.library.collectAsStateWithLifecycle(Library(emptyMap()))
    val topAppBarScrollState = rememberTopAppBarScrollState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior(topAppBarScrollState) }

    val pagerState = rememberPagerState(initialPage = 0)

    val systemUiController = rememberSystemUiController()
    val statusBarColor = when {
      scrollBehavior.scrollFraction > 0 -> TopAppBarDefaults
        .smallTopAppBarColors()
        .containerColor(scrollBehavior.scrollFraction)
        .value
      else -> MaterialTheme.colorScheme.surface
    }

    SideEffect {
      systemUiController.setStatusBarColor(
        color = statusBarColor
      )
    }

    if (showCreateBookSheet) {
      CreateBookSheet(
        onScanBarcodeClick = { navigator.push(BarcodeScannerScreen()) },
        onIsbnSearchClick = { navigator.push(IsbnLookupScreen()) },
        onFillManuallyClick = { navigator.push(ManageBookScreen()) },
        onDismiss = { showCreateBookSheet = false }
      )
    }

    Scaffold(
      modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
      topBar = {
        SmallTopAppBar(
          scrollBehavior = scrollBehavior,
          modifier = Modifier.statusBarsPadding(),
          title = { Text(stringResource(R.string.library)) },
          actions = {
            IconButton(onClick = { /*TODO*/ }) {
              Icon(
                Icons.Default.Search,
                contentDescription = stringResource(R.string.action_search)
              )
            }
          }
        )
      },
      content = { innerPadding ->
        Crossfade(
          targetState = library.groups.isEmpty(),
          modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
        ) { isEmpty ->
          if (isEmpty) {
            NoItemsFound(
              modifier = Modifier.fillMaxSize(),
              icon = Icons.Outlined.Book
            )
          } else {
            Column(
              modifier = Modifier.fillMaxSize(),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              // TODO: Fix the tab row width when having a few items.
              ScrollableTabRow(
                modifier = Modifier.fillMaxWidth(),
                selectedTabIndex = pagerState.currentPage,
                edgePadding = 12.dp,
                containerColor = statusBarColor,
                indicator = { tabPositions ->
                  TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage])
                  )
                },
              ) {
                library.groups.keys.forEachIndexed { index, group ->
                  Tab(
                    text = { Text(group.name) },
                    selected = pagerState.currentPage == index,
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onBackground,
                    onClick = {
                      scope.launch { pagerState.animateScrollToPage(index) }
                    }
                  )
                }
              }
              // TODO: Remove it when Material3 fixes the scrollable tab row divider width issue.
              TabRowDefaults.Divider(
                modifier = Modifier.offset(y = (-1).dp)
              )
              HorizontalPager(
                state = pagerState,
                count = library.groups.keys.size,
                verticalAlignment = Alignment.Top,
              ) { page ->
                LibraryGrid(
                  modifier = Modifier.fillMaxSize(),
                  books = library.groups.values.toList()[page],
                  onBookClick = { book ->
                    navigator.push(BookScreen(book.id))
                  }
                )
              }
            }
          }
        }
      },
      floatingActionButtonPosition = FabPosition.End,
      floatingActionButton = {
        FloatingActionButton(
          onClick = { showCreateBookSheet = true }
        ) {
          Icon(
            Icons.Default.Add,
            contentDescription = stringResource(R.string.action_new_book)
          )
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
          CreateBookSheetItem(
            icon = Icons.Outlined.QrCodeScanner,
            text = stringResource(R.string.action_scan_barcode),
            onClick = {
              onScanBarcodeClick()
              onDismiss()
            }
          )
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
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(vertical = 16.dp, horizontal = 24.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = text,
        tint = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }

}
