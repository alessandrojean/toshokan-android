package io.github.alessandrojean.toshokan.presentation.ui.isbnlookup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.extensions.surfaceWithTonalElevation
import io.github.alessandrojean.toshokan.presentation.ui.book.BookScreen
import io.github.alessandrojean.toshokan.presentation.ui.book.manage.ManageBookScreen
import io.github.alessandrojean.toshokan.presentation.ui.core.components.BoxedCircularProgressIndicator
import io.github.alessandrojean.toshokan.presentation.ui.core.components.NoItemsFound
import io.github.alessandrojean.toshokan.presentation.ui.isbnlookup.components.HistoryList
import io.github.alessandrojean.toshokan.presentation.ui.isbnlookup.components.IsbnLookupResultList
import io.github.alessandrojean.toshokan.presentation.ui.theme.DividerOpacity
import io.github.alessandrojean.toshokan.util.isValidIsbn
import kotlinx.coroutines.android.awaitFrame

data class IsbnLookupScreen(val isbn: String? = null) : AndroidScreen() {

  @Composable
  override fun Content() {
    val isbnLookupViewModel = getViewModel<IsbnLookupViewModel>()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior() }
    val listState = rememberLazyListState()
    val navigator = LocalNavigator.currentOrThrow
    val history by isbnLookupViewModel.history.collectAsState(emptySet())

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
        }
      }
    )

    val systemUiController = rememberSystemUiController()
    val statusBarColor = MaterialTheme.colorScheme.surfaceWithTonalElevation(6.dp)

    SideEffect {
      systemUiController.setStatusBarColor(
        color = statusBarColor
      )
    }

    var showDuplicateDialog by remember { mutableStateOf(false) }
    var duplicateId by remember { mutableStateOf<Long?>(null) }

    DuplicateDialog(
      visible = showDuplicateDialog,
      onDismiss = { showDuplicateDialog = false },
      onCreateDuplicate = { showDuplicateDialog = false },
      onViewBook = {
        navigator.replace(BookScreen(bookId = duplicateId!!))
        showDuplicateDialog = false
      }
    )

    Scaffold(
      modifier = Modifier
        .nestedScroll(scrollBehavior.nestedScrollConnection)
        .systemBarsPadding(),
      topBar = {
        CreateBookTopBar(
          backgroundColor = statusBarColor,
          progress = when (isbnLookupViewModel.state) {
            IsbnLookupState.RESULTS -> {
              isbnLookupViewModel.progress.takeIf { it != 1f }
            }
            else -> null
          },
          searchText = isbnLookupViewModel.searchQuery,
          placeholderText = stringResource(R.string.isbn_search_placeholder),
          scrollBehavior = scrollBehavior,
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
            isbnLookupViewModel.search()
            isbnLookupViewModel.checkDuplicates()?.let {
              duplicateId = it
              showDuplicateDialog = true
            }
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
                  .imePadding()
              )
            }
            IsbnLookupState.HISTORY -> {
              HistoryList(
                modifier = Modifier.fillMaxSize(),
                history = history.toList(),
                contentPadding = innerPadding,
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
                  .imePadding()
              )
            }
            IsbnLookupState.NO_RESULTS -> {
              NoItemsFound(
                text = stringResource(R.string.no_results_found),
                icon = Icons.Outlined.SearchOff,
                modifier = Modifier
                  .fillMaxSize()
                  .padding(innerPadding)
                  .imePadding()
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
                  .imePadding()
              )
            }
            IsbnLookupState.RESULTS -> {
              IsbnLookupResultList(
                results = isbnLookupViewModel.results,
                modifier = Modifier
                  .fillMaxSize()
                  .padding(innerPadding)
                  .imePadding(),
                contentPadding = PaddingValues(12.dp),
                listState = listState,
                onResultClick = { result ->
                  isbnLookupViewModel.cancelSearch()
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
  fun CreateBookTopBar(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    searchText: String = "",
    progress: Float? = null,
    placeholderText: String = "",
    scrollBehavior: TopAppBarScrollBehavior,
    shouldRequestFocus: Boolean = true,
    onNavigationClick: () -> Unit,
    onClearClick: () -> Unit = {},
    onSearchTextChanged: (String) -> Unit = {},
    onSearchAction: () -> Unit = {}
  ) {
    var showClearButton by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    val animatedProgress by animateFloatAsState(
      targetValue = if (progress != null && progress < 1f) progress else 0f,
      animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )

    Column(
      modifier = Modifier
        .background(backgroundColor)
        .fillMaxWidth()
        .then(modifier)
    ) {
      SmallTopAppBar(
        colors = TopAppBarDefaults.smallTopAppBarColors(
          containerColor = backgroundColor,
          scrolledContainerColor = backgroundColor
        ),
        scrollBehavior = scrollBehavior,
        navigationIcon = {
          IconButton(onClick = onNavigationClick) {
            Icon(
              Icons.Default.ArrowBack,
              contentDescription = stringResource(R.string.action_back)
            )
          }
        },
        title = {
          OutlinedTextField(
            modifier = Modifier
              .fillMaxWidth()
              .focusRequester(focusRequester)
              .onFocusChanged { focusState ->
                showClearButton = (focusState.isFocused)
                if (focusState.isFocused) {
                  keyboardController?.show()
                }
              },
            value = searchText,
            onValueChange = onSearchTextChanged,
            placeholder = {
              Text(
                text = placeholderText,
                style = MaterialTheme.typography.titleLarge
              )
            },
            colors = TextFieldDefaults.textFieldColors(
              focusedIndicatorColor = Color.Transparent,
              unfocusedIndicatorColor = Color.Transparent,
              containerColor = Color.Transparent,
              cursorColor = LocalContentColor.current
            ),
            textStyle = MaterialTheme.typography.titleLarge.copy(
              color = LocalContentColor.current
            ),
            maxLines = 1,
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
              imeAction = ImeAction.Search,
              keyboardType = KeyboardType.Number
            ),
            keyboardActions = KeyboardActions(
              onSearch = {
                keyboardController?.hide()
                focusRequester.freeFocus()
                onSearchAction()
              }
            )
          )
        },
        actions = {
          AnimatedVisibility(
            visible = showClearButton && searchText.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
          ) {
            IconButton(onClick = { onClearClick() }) {
              Icon(
                imageVector = Icons.Outlined.Cancel,
                contentDescription = stringResource(R.string.action_clear),
                tint = LocalContentColor.current
              )
            }
          }
        }
      )

      LinearProgressIndicator(
        modifier = Modifier
          .fillMaxWidth()
          .height(2.dp),
        progress = animatedProgress,
        trackColor = LocalContentColor.current.copy(alpha = DividerOpacity)
      )
    }

    LaunchedEffect(Unit) {
      if (shouldRequestFocus) {
        awaitFrame()
        focusRequester.requestFocus()
      }
    }
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
