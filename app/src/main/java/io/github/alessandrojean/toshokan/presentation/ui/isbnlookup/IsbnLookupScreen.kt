package io.github.alessandrojean.toshokan.presentation.ui.isbnlookup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.ui.core.components.BoxedCircularProgressIndicator
import io.github.alessandrojean.toshokan.presentation.ui.core.components.NoItemsFound
import io.github.alessandrojean.toshokan.presentation.ui.isbnlookup.components.IsbnLookupResultList

class IsbnLookupScreen : Screen {

  @Composable
  override fun Content() {
    val createBookViewModel = getViewModel<IsbnLookupViewModel>()
    val uiState by createBookViewModel.uiState.collectAsState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior() }
    val listState = rememberLazyListState()
    val navigator = LocalNavigator.currentOrThrow

    val expandedFab by remember {
      derivedStateOf {
        listState.firstVisibleItemIndex == 0
      }
    }

    Scaffold(
      modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
      topBar = {
        CreateBookTopBar(
          modifier = Modifier.statusBarsPadding(),
          searchText = uiState.searchQuery,
          placeholderText = stringResource(R.string.isbn_search_placeholder),
          scrollBehavior = scrollBehavior,
          onNavigationClick = { navigator.pop() },
          onClearClick = { createBookViewModel.onSearchQueryChange("") },
          onSearchTextChanged = { createBookViewModel.onSearchQueryChange(it) },
          onSearchAction = { createBookViewModel.search() }
        )
      },
      floatingActionButton = {
        AnimatedVisibility(
          visible = uiState.selected != null,
          enter = fadeIn(),
          exit = fadeOut()
        ) {
          ExtendedFloatingActionButton(
            onClick = { /* TODO */ },
            expanded = expandedFab,
            icon = {
              Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = stringResource(R.string.action_select)
              )
            },
            text = { Text(stringResource(R.string.action_select)) }
          )
        }
      },
      content = { innerPadding ->
        when {
          uiState.loading -> BoxedCircularProgressIndicator(Modifier.padding(innerPadding))
          uiState.results.isEmpty() && uiState.searchedOnce -> NoItemsFound(
            text = stringResource(R.string.no_results_found),
            icon = Icons.Outlined.SearchOff,
            modifier = Modifier.padding(innerPadding)
          )
          else -> IsbnLookupResultList(
            results = uiState.results,
            selected = uiState.selected,
            modifier = Modifier
              .fillMaxSize()
              .padding(8.dp),
            contentPadding = innerPadding,
            listState = listState,
            onResultClick = { result ->
              createBookViewModel.onSelectedChange(result)
            }
          )
        }
      },
      bottomBar = {
        Spacer(modifier = Modifier.navigationBarsPadding())
      }
    )
  }

  @Composable
  fun CreateBookTopBar(
    modifier: Modifier = Modifier,
    searchText: String = "",
    placeholderText: String = "",
    scrollBehavior: TopAppBarScrollBehavior,
    onNavigationClick: () -> Unit,
    onClearClick: () -> Unit = {},
    onSearchTextChanged: (String) -> Unit = {},
    onSearchAction: () -> Unit = {}
  ) {
    var showClearButton by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    SmallTopAppBar(
      modifier = modifier,
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
            .onFocusChanged { focusState ->
              showClearButton = (focusState.isFocused)
            }
            .focusRequester(focusRequester),
          value = searchText,
          onValueChange = onSearchTextChanged,
          placeholder = { Text(placeholderText) },
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

    LaunchedEffect(Unit) {
      focusRequester.requestFocus()
    }
  }

}
