package io.github.alessandrojean.toshokan.presentation.ui.isbnlookup

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.ManageSearch
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.extensions.surfaceWithTonalElevation
import io.github.alessandrojean.toshokan.presentation.ui.book.BookScreen
import io.github.alessandrojean.toshokan.presentation.ui.book.manage.ManageBookScreen
import io.github.alessandrojean.toshokan.presentation.ui.core.components.BoxedCircularProgressIndicator
import io.github.alessandrojean.toshokan.presentation.ui.core.components.NoItemsFound
import io.github.alessandrojean.toshokan.presentation.ui.core.components.SearchTopAppBar
import io.github.alessandrojean.toshokan.presentation.ui.isbnlookup.components.HistoryList
import io.github.alessandrojean.toshokan.presentation.ui.isbnlookup.components.IsbnLookupResultList
import io.github.alessandrojean.toshokan.presentation.ui.settings.search.SearchSettingsScreen
import io.github.alessandrojean.toshokan.presentation.ui.theme.DividerOpacity
import io.github.alessandrojean.toshokan.util.ConnectionState
import io.github.alessandrojean.toshokan.util.connectivityState
import io.github.alessandrojean.toshokan.util.extension.bottom
import io.github.alessandrojean.toshokan.util.extension.bottomPadding
import io.github.alessandrojean.toshokan.util.extension.collectAsStateWithLifecycle
import io.github.alessandrojean.toshokan.util.extension.copy
import io.github.alessandrojean.toshokan.util.extension.end
import io.github.alessandrojean.toshokan.util.extension.navigationBarsWithIme
import io.github.alessandrojean.toshokan.util.extension.navigationBarsWithImePadding
import io.github.alessandrojean.toshokan.util.extension.start
import io.github.alessandrojean.toshokan.util.extension.top
import io.github.alessandrojean.toshokan.util.isValidIsbn

data class IsbnLookupScreen(val isbn: String? = null) : AndroidScreen() {

  @Composable
  override fun Content() {
    val isbnLookupViewModel = getViewModel<IsbnLookupViewModel>()
    val topAppBarScrollState = rememberTopAppBarScrollState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior(topAppBarScrollState) }
    val listState = rememberLazyListState()
    val navigator = LocalNavigator.currentOrThrow
    val history by isbnLookupViewModel.history.collectAsStateWithLifecycle(emptySet())

    var showDuplicateDialog by remember { mutableStateOf(false) }
    var duplicateId by remember { mutableStateOf<Long?>(null) }

    LifecycleEffect(
      onStarted = {
        if (
          !isbn.isNullOrBlank() &&
          isbn.isValidIsbn() &&
          isbnLookupViewModel.searchQuery != isbn &&
          isbnLookupViewModel.results.isEmpty()
        ) {
          isbnLookupViewModel.searchQuery = isbn
          isbnLookupViewModel.search()
          isbnLookupViewModel.checkDuplicates()?.let {
            duplicateId = it
            showDuplicateDialog = true
          }
        }
      },
      onDisposed = {
        isbnLookupViewModel.cancelSearch()
      }
    )

    DuplicateDialog(
      visible = showDuplicateDialog,
      onDismiss = { showDuplicateDialog = false },
      onCreateDuplicate = { showDuplicateDialog = false },
      onViewBook = {
        navigator.replace(BookScreen(bookId = duplicateId!!))
        showDuplicateDialog = false
      }
    )

    val progress = when (isbnLookupViewModel.state) {
      IsbnLookupState.RESULTS -> {
        isbnLookupViewModel.progress.takeIf { it != 1f }
      }
      else -> null
    }

    val animatedProgress by animateFloatAsState(
      targetValue = if (progress != null && progress < 1f) progress else 0f,
      animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )

    val topAppBarBackground = MaterialTheme.colorScheme.surfaceWithTonalElevation(6.dp)

    val internetConnection by connectivityState()

    Scaffold(
      modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
      topBar = {
        SearchTopAppBar(
          backgroundColor = topAppBarBackground,
          searchText = isbnLookupViewModel.searchQuery,
          placeholderText = stringResource(R.string.isbn_search_placeholder),
          scrollBehavior = scrollBehavior,
          keyboardType = KeyboardType.Number,
          shouldRequestFocus = isbn.isNullOrBlank() || !isbn.isValidIsbn(),
          onNavigationClick = { navigator.pop() },
          onClearClick = {
            isbnLookupViewModel.searchQuery = ""
            isbnLookupViewModel.state = if (history.isNotEmpty()) {
              IsbnLookupState.HISTORY
            } else {
              IsbnLookupState.EMPTY
            }
          },
          onSearchTextChanged = { isbnLookupViewModel.searchQuery = it },
          onSearchAction = {
            if (internetConnection is ConnectionState.Available) {
              isbnLookupViewModel.search()
              isbnLookupViewModel.checkDuplicates()?.let {
                duplicateId = it
                showDuplicateDialog = true
              }
            }
          },
          actions = {
            IconButton(onClick = { navigator.push(SearchSettingsScreen()) }) {
              Icon(
                imageVector = Icons.Outlined.ManageSearch,
                contentDescription = stringResource(R.string.settings)
              )
            }
          },
          bottomContent = {
            LinearProgressIndicator(
              modifier = Modifier
                .fillMaxWidth()
                .height(2.dp),
              progress = animatedProgress,
              trackColor = LocalContentColor.current.copy(alpha = DividerOpacity)
            )
          }
        )
      },
      content = { innerPadding ->
        Crossfade(
          targetState = isbnLookupViewModel.state,
          modifier = Modifier.fillMaxSize()
        ) { state ->
          when (state) {
            IsbnLookupState.EMPTY -> {
              NoItemsFound(
                icon = Icons.Outlined.Search,
                modifier = Modifier
                  .fillMaxSize()
                  .padding(innerPadding)
                  .navigationBarsWithImePadding()
              )
            }
            IsbnLookupState.NO_INTERNET -> {
              NoItemsFound(
                icon = Icons.Outlined.CloudOff,
                text = stringResource(R.string.no_internet_connection),
                modifier = Modifier
                  .fillMaxSize()
                  .padding(innerPadding)
                  .navigationBarsWithImePadding()
              )
            }
            IsbnLookupState.HISTORY -> {
              HistoryList(
                modifier = Modifier.fillMaxSize(),
                history = history.toList(),
                contentPadding = innerPadding.copy(
                  bottom = innerPadding.bottom + WindowInsets.navigationBarsWithIme.bottomPadding
                ),
                onClick = {
                  isbnLookupViewModel.searchQuery = it
                  isbnLookupViewModel.search()
                },
                onRemoveClick = {
                  isbnLookupViewModel.removeHistoryItem(it)
                }
              )
            }
            IsbnLookupState.LOADING -> {
              BoxedCircularProgressIndicator(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(innerPadding)
                  .navigationBarsWithImePadding()
              )
            }
            IsbnLookupState.NO_RESULTS -> {
              NoItemsFound(
                text = stringResource(R.string.no_results_found),
                icon = Icons.Outlined.SearchOff,
                modifier = Modifier
                  .fillMaxSize()
                  .padding(innerPadding)
                  .navigationBarsWithImePadding()
              )
            }
            IsbnLookupState.ERROR -> {
              NoItemsFound(
                text = isbnLookupViewModel.error?.localizedMessage
                  ?: stringResource(R.string.error_happened),
                icon = Icons.Outlined.Error,
                modifier = Modifier
                  .fillMaxSize()
                  .padding(innerPadding)
                  .navigationBarsWithImePadding()
              )
            }
            IsbnLookupState.RESULTS -> {
              IsbnLookupResultList(
                results = isbnLookupViewModel.results,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                  top = 12.dp + innerPadding.top,
                  start = 12.dp + innerPadding.start,
                  end = 12.dp + innerPadding.end,
                  bottom = 12.dp + WindowInsets.navigationBarsWithIme.bottomPadding
                ),
                listState = listState,
                onResultClick = { result ->
                  navigator.push(ManageBookScreen(result))
                }
              )
            }
          }
        }
      }
    )
  }

  @Composable
  fun DuplicateDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onViewBook: () -> Unit,
    onCreateDuplicate: () -> Unit
  ) {
    if (visible) {
      AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.book_duplicate_title)) },
        text = { Text(stringResource(R.string.book_duplicate_warning)) },
        dismissButton = {
          TextButton(
            onClick = {
              onCreateDuplicate.invoke()
              onDismiss.invoke()
            }
          ) {
            Text(stringResource(R.string.action_create_duplicate))
          }
        },
        confirmButton = {
          TextButton(
            onClick = {
              onViewBook.invoke()
              onDismiss.invoke()
            }
          ) {
            Text(stringResource(R.string.action_view_book))
          }
        }
      )
    }
  }

}
