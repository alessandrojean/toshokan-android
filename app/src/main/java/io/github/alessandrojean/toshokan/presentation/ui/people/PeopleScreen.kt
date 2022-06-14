package io.github.alessandrojean.toshokan.presentation.ui.people

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
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
import io.github.alessandrojean.toshokan.database.data.Person
import io.github.alessandrojean.toshokan.presentation.ui.core.components.NoItemsFound
import io.github.alessandrojean.toshokan.presentation.ui.core.components.SelectionTopAppBar
import io.github.alessandrojean.toshokan.presentation.ui.people.manage.ManagePeopleMode
import io.github.alessandrojean.toshokan.presentation.ui.people.manage.ManagePeopleScreen
import io.github.alessandrojean.toshokan.util.extension.collectAsStateWithLifecycle

class PeopleScreen : AndroidScreen() {

  @Composable
  override fun Content() {
    val peopleViewModel = getViewModel<PeopleViewModel>()
    val uiState by peopleViewModel.uiState.collectAsStateWithLifecycle()
    val people by peopleViewModel.people.collectAsStateWithLifecycle(emptyList())
    val topAppBarScrollState = rememberTopAppBarScrollState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior(topAppBarScrollState) }
    val listState = rememberLazyListState()
    val navigator = LocalNavigator.currentOrThrow
    val expandedFab by remember {
      derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }
    val selectionMode by remember {
      derivedStateOf { uiState.selected.isNotEmpty() }
    }

    val topAppBarBackgroundColors = TopAppBarDefaults.smallTopAppBarColors()
    val topAppBarBackground = topAppBarBackgroundColors.containerColor(scrollBehavior.scrollFraction).value

    BackHandler(enabled = selectionMode) {
      peopleViewModel.clearSelection()
    }

    LaunchedEffect(navigator.lastItem) {
      if (navigator.lastItem is PeopleScreen) {
        peopleViewModel.clearSelection()
      }
    }

    if (uiState.showDeleteWarning) {
      DeletePeopleWarningDialog(
        selectedCount = uiState.selected.size,
        onClose = { peopleViewModel.hideDeleteWarning() },
        onConfirmClick = {
          peopleViewModel.deleteSelected()
          peopleViewModel.hideDeleteWarning()
        },
        onDismissClick = { peopleViewModel.hideDeleteWarning() }
      )
    }

    Scaffold(
      modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
      topBar = {
        Crossfade(targetState = selectionMode) { selection ->
          if (selection) {
            SelectionTopAppBar(
              selectionCount = uiState.selected.size,
              onClearSelectionClick = { peopleViewModel.clearSelection() },
              onEditClick = {
                navigator.push(
                  ManagePeopleScreen(
                    mode = ManagePeopleMode.EDIT,
                    person = people.firstOrNull { it.id == uiState.selected.firstOrNull() }
                  )
                )
              },
              onDeleteClick = { peopleViewModel.showDeleteWarning() },
              scrollBehavior = scrollBehavior
            )
          } else {
            Surface(color = topAppBarBackground) {
              SmallTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                colors = TopAppBarDefaults.smallTopAppBarColors(
                  containerColor = Color.Transparent,
                  scrolledContainerColor = Color.Transparent
                ),
                navigationIcon = {
                  IconButton(onClick = { navigator.pop() }) {
                    Icon(
                      Icons.Default.ArrowBack,
                      contentDescription = stringResource(R.string.action_back)
                    )
                  }
                },
                title = { Text(stringResource(R.string.people)) },
                scrollBehavior = scrollBehavior
              )
            }
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
            onClick = { navigator.push(ManagePeopleScreen()) },
            expanded = expandedFab,
            icon = {
              Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.create_person)
              )
            },
            text = { Text(stringResource(R.string.create_person)) }
          )
        }
      },
      content = { innerPadding ->
        if (people.isEmpty()) {
          NoItemsFound(
            modifier = Modifier.padding(innerPadding),
            text = stringResource(R.string.no_people_found),
            icon = Icons.Outlined.Group
          )
        } else {
          LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier.selectableGroup(),
            state = listState,
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
          ) {
            items(people) { person ->
              PersonItem(
                modifier = Modifier.fillMaxWidth(),
                person = person,
                selected = person.id in uiState.selected,
                onClick = {
                  if (selectionMode) {
                    peopleViewModel.toggleSelection(person.id)
                  }
                },
                onLongClick = { peopleViewModel.toggleSelection(person.id) }
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
  fun DeletePeopleWarningDialog(
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
            R.plurals.person_delete_warning,
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
  fun PersonItem(
    modifier: Modifier = Modifier,
    person: Person,
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
            MaterialTheme.colorScheme.surfaceVariant
          } else {
            MaterialTheme.colorScheme.surface
          }
        )
        .padding(16.dp)
    ) {
      Text(text = person.name)
    }
  }

}
