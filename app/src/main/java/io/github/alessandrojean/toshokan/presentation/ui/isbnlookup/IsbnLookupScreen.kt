package io.github.alessandrojean.toshokan.presentation.ui.isbnlookup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.ui.book.manage.ManageBookScreen
import io.github.alessandrojean.toshokan.presentation.ui.core.components.BoxedCircularProgressIndicator
import io.github.alessandrojean.toshokan.presentation.ui.core.components.NoItemsFound
import io.github.alessandrojean.toshokan.presentation.ui.isbnlookup.components.IsbnLookupResultList
import io.github.alessandrojean.toshokan.util.isValidIsbn
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch

data class IsbnLookupScreen(val isbn: String? = null) : AndroidScreen() {

  @Composable
  override fun Content() {
    val isbnLookupViewModel = getViewModel<IsbnLookupViewModel>()
    val uiState by isbnLookupViewModel.uiState.collectAsState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior() }
    val listState = rememberLazyListState()
    val navigator = LocalNavigator.currentOrThrow

    if (
      !isbn.isNullOrBlank() &&
      isbn.isValidIsbn() &&
      uiState.searchQuery != isbn &&
      isbnLookupViewModel.results.isEmpty()
    ) {
      isbnLookupViewModel.onSearchQueryChange(isbn)
      isbnLookupViewModel.search()
    }

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

    Scaffold(
      modifier = Modifier
        .nestedScroll(scrollBehavior.nestedScrollConnection)
        .systemBarsPadding(),
      topBar = {
        CreateBookTopBar(
          backgroundColor = statusBarColor,
          progress = if (uiState.progress > 0f && uiState.progress < 1f) uiState.progress else null,
          searchText = uiState.searchQuery,
          placeholderText = stringResource(R.string.isbn_search_placeholder),
          scrollBehavior = scrollBehavior,
          shouldRequestFocus = isbn.isNullOrBlank() || !isbn.isValidIsbn(),
          onNavigationClick = { navigator.pop() },
          onClearClick = { isbnLookupViewModel.onSearchQueryChange("") },
          onSearchTextChanged = { isbnLookupViewModel.onSearchQueryChange(it) },
          onSearchAction = {
            isbnLookupViewModel.search()
          }
        )
      },
      content = { innerPadding ->
        when {
          uiState.loading && isbnLookupViewModel.results.isEmpty() -> {
            BoxedCircularProgressIndicator(
              modifier = Modifier
                .padding(innerPadding)
                .imePadding()
            )
          }
          isbnLookupViewModel.results.isEmpty() && uiState.searchedOnce && !uiState.loading -> {
            NoItemsFound(
              text = stringResource(R.string.no_results_found),
              icon = Icons.Outlined.SearchOff,
              modifier = Modifier
                .padding(innerPadding)
                .imePadding()
            )
          }
          isbnLookupViewModel.results.isEmpty() && !uiState.searchedOnce && !uiState.loading -> {
            NoItemsFound(
              icon = Icons.Outlined.Search,
              modifier = Modifier
                .padding(innerPadding)
                .imePadding()
            )
          }
          else -> IsbnLookupResultList(
            results = isbnLookupViewModel.results,
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
              .imePadding(),
            contentPadding = PaddingValues(12.dp),
            listState = listState,
            onResultClick = { result ->
              navigator.push(ManageBookScreen(result))
            }
          )
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
      targetValue = progress ?: 0f,
      animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )
    val progressOpacity by animateFloatAsState(if (progress != null) 1f else 0f)

    Column(
      modifier = Modifier
        .background(backgroundColor)
        .fillMaxWidth()
        .then(modifier)
    ) {
      SmallTopAppBar(
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
              color = MaterialTheme.colorScheme.onSurface
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
            visible = showClearButton,
            enter = fadeIn(),
            exit = fadeOut()
          ) {
            IconButton(onClick = { onClearClick() }) {
              Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.action_clear),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      )

      LinearProgressIndicator(
        modifier = Modifier
          .fillMaxWidth()
          .graphicsLayer(alpha = progressOpacity),
        progress = animatedProgress
      )
    }

    LaunchedEffect(Unit) {
      if (shouldRequestFocus) {
        awaitFrame()
        focusRequester.requestFocus()
      }
    }
  }

}
