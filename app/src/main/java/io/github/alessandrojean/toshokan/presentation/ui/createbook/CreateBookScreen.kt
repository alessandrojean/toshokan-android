package io.github.alessandrojean.toshokan.presentation.ui.createbook

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.ui.createbook.components.CreateBookResultList

@Composable
fun CreateBookScreen(
  navController: NavController,
  createBookViewModel: CreateBookViewModel = viewModel()
) {
  val uiState by createBookViewModel.uiState.collectAsState()
  val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior() }

  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      CreateBookTopBar(
        searchText = uiState.searchQuery,
        placeholderText = stringResource(R.string.isbn_search_placeholder),
        scrollBehavior = scrollBehavior,
        onNavigationClick = { navController.popBackStack() },
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
        FloatingActionButton(onClick = { /* TODO */ }) {
          Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = stringResource(R.string.action_check)
          )
        }
      }
    },
    floatingActionButtonPosition = FabPosition.End,
    content = { innerPadding ->
      Box(
        modifier = Modifier
          .padding(innerPadding)
          .fillMaxSize()
      ) {
        Column(
          modifier = Modifier
            .padding(8.dp)
            .fillMaxSize(),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          when {
            uiState.loading -> CircularProgressIndicator()
            uiState.results.isEmpty() && uiState.searchedOnce -> {
              Text(
                text = stringResource(R.string.no_results_found),
                style = MaterialTheme.typography.bodyMedium
              )
            }
            else -> CreateBookResultList(
              results = uiState.results,
              selected = uiState.selected,
              modifier = Modifier.fillMaxSize(),
              onResultClick = { result ->
                createBookViewModel.onSelectedChange(result)
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
    scrollBehavior = scrollBehavior,
    navigationIcon = {
      IconButton(onClick = onNavigationClick) {
        Icon(
          Icons.Default.ArrowBack,
          contentDescription = stringResource(R.string.action_back),
          tint = MaterialTheme.colorScheme.onSurfaceVariant
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
        placeholder = {
          Text(placeholderText)
        },
        colors = TextFieldDefaults.textFieldColors(
          focusedIndicatorColor = Color.Transparent,
          unfocusedIndicatorColor = Color.Transparent,
          containerColor = Color.Transparent,
          cursorColor = LocalContentColor.current
        ),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
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