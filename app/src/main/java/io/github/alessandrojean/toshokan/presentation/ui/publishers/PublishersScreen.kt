package io.github.alessandrojean.toshokan.presentation.ui.publishers

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.Publisher
import io.github.alessandrojean.toshokan.presentation.extensions.selection
import io.github.alessandrojean.toshokan.presentation.ui.core.components.EnhancedSmallTopAppBar
import io.github.alessandrojean.toshokan.presentation.ui.core.components.NoItemsFound
import io.github.alessandrojean.toshokan.presentation.ui.core.components.SelectionTopAppBar
import io.github.alessandrojean.toshokan.presentation.ui.people.PeopleScreen
import io.github.alessandrojean.toshokan.presentation.ui.publishers.manage.ManagePublisherMode
import io.github.alessandrojean.toshokan.presentation.ui.publishers.manage.ManagePublisherScreen
import io.github.alessandrojean.toshokan.util.extension.collectAsStateWithLifecycle

class PublishersScreen : AndroidScreen() {

  @Composable
  override fun Content() {
    val publishersViewModel = getViewModel<PublishersViewModel>()
    val navigator = LocalNavigator.currentOrThrow
    val uiState by publishersViewModel.uiState.collectAsStateWithLifecycle()
    val topAppBarScrollState = rememberTopAppBarScrollState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior(topAppBarScrollState) }
    val listState = rememberLazyListState()
    val expandedFab by remember {
      derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }
    val selectionMode by remember {
      derivedStateOf { uiState.selected.isNotEmpty() }
    }

    val publishers by publishersViewModel.publishers.collectAsStateWithLifecycle(emptyList())

    BackHandler(enabled = selectionMode) {
      publishersViewModel.clearSelection()
    }

    LaunchedEffect(navigator.lastItem) {
      if (navigator.lastItem is PeopleScreen) {
        publishersViewModel.clearSelection()
      }
    }

    if (uiState.showDeleteWarning) {
      DeletePublishersWarningDialog(
        selectedCount = uiState.selected.size,
        onClose = { publishersViewModel.hideDeleteWarning() },
        onConfirmClick = {
          publishersViewModel.deleteSelected()
          publishersViewModel.hideDeleteWarning()
        },
        onDismissClick = { publishersViewModel.hideDeleteWarning() }
      )
    }

    Scaffold(
      modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
      topBar = {
        Crossfade(
          modifier = Modifier.background(MaterialTheme.colorScheme.background),
          targetState = selectionMode
        ) { selection ->
          if (selection) {
            SelectionTopAppBar(
              selectionCount = uiState.selected.size,
              onClearSelectionClick = { publishersViewModel.clearSelection() },
              onEditClick = {
                navigator.push(
                  ManagePublisherScreen(
                    mode = ManagePublisherMode.EDIT,
                    publisher = publishers.firstOrNull { it.id == uiState.selected.firstOrNull() }
                  )
                )
              },
              onDeleteClick = { publishersViewModel.showDeleteWarning() },
              scrollBehavior = scrollBehavior
            )
          } else {
            EnhancedSmallTopAppBar(
              contentPadding = WindowInsets.statusBars.asPaddingValues(),
              navigationIcon = {
                IconButton(onClick = { navigator.pop() }) {
                  Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.action_back)
                  )
                }
              },
              title = { Text(stringResource(R.string.publishers)) },
              scrollBehavior = scrollBehavior
            )
          }
        }
      },
      floatingActionButton = {
        AnimatedVisibility(
          visible = !selectionMode,
          enter = fadeIn(),
          exit = fadeOut()
        ) {
          ExtendedFloatingActionButton(
            onClick = { navigator.push(ManagePublisherScreen()) },
            expanded = expandedFab,
            icon = {
              Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.create_publisher)
              )
            },
            text = { Text(stringResource(R.string.create_publisher)) }
          )
        }
      },
      content = { innerPadding ->
        if (publishers.isEmpty()) {
          NoItemsFound(
            modifier = Modifier.padding(innerPadding),
            text = stringResource(R.string.no_publishers_found),
            icon = Icons.Outlined.Domain
          )
        } else {
          LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier.selectableGroup(),
            state = listState,
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
          ) {
            items(publishers) { publisher ->
              PublisherItem(
                modifier = Modifier.fillMaxWidth(),
                publisher = publisher,
                selected = publisher.id in uiState.selected,
                onClick = {
                  if (selectionMode) {
                    publishersViewModel.toggleSelection(publisher.id)
                  }
                },
                onLongClick = { publishersViewModel.toggleSelection(publisher.id) }
              )
            }
          }
        }
      },
      bottomBar = {
        Spacer(
          modifier = Modifier.windowInsetsPadding(
            WindowInsets.systemBars.only(
              WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
            )
          )
        )
      }
    )
  }

  @Composable
  fun DeletePublishersWarningDialog(
    selectedCount: Int,
    onClose: () -> Unit = {},
    onConfirmClick: () -> Unit = {},
    onDismissClick: () -> Unit = {}
  ) {
    AlertDialog(
      onDismissRequest = onClose,
      text = {
        Text(
          text = pluralStringResource(
            R.plurals.publisher_delete_warning,
            selectedCount
          )
        )
      },
      confirmButton = {
        TextButton(onClick = onConfirmClick) {
          Text(stringResource(R.string.action_delete))
        }
      },
      dismissButton = {
        TextButton(onClick = onDismissClick) {
          Text(stringResource(R.string.action_cancel))
        }
      }
    )
  }

  @Composable
  fun PublisherItem(
    modifier: Modifier = Modifier,
    publisher: Publisher,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
  ) {
    Row(
      modifier = modifier
        .combinedClickable(
          onClick = onClick,
          onLongClick = onLongClick,
          role = Role.Checkbox
        )
        .background(
          color = if (selected) {
            MaterialTheme.colorScheme.selection
          } else {
            MaterialTheme.colorScheme.surface
          }
        )
        .padding(16.dp)
    ) {
      Text(text = publisher.name)
    }
  }

}

