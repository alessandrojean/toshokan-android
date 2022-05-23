package io.github.alessandrojean.toshokan.presentation.ui.people

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
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
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.Person
import io.github.alessandrojean.toshokan.presentation.ui.people.manage.ManagePeopleMode
import io.github.alessandrojean.toshokan.presentation.ui.people.manage.ManagePeopleScreen

@Composable
fun PeopleScreen(
  navController: NavController,
  peopleViewModel: PeopleViewModel
) {
  val uiState by peopleViewModel.uiState.collectAsState()
  val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior() }
  val listState = rememberLazyListState()
  val expandedFab by remember {
    derivedStateOf {
      listState.firstVisibleItemIndex == 0
    }
  }
  val selectionMode by remember {
    derivedStateOf {
      uiState.selected.isNotEmpty()
    }
  }

  val people by uiState.people.collectAsState(emptyList())

  if (uiState.showManageDialog) {
    Dialog(
      onDismissRequest = { peopleViewModel.hideManageDialog() },
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      Surface(modifier = Modifier.fillMaxWidth()) {
        ManagePeopleScreen(
          mode = uiState.manageDialogMode,
          person = people.firstOrNull { it.id == uiState.selected.firstOrNull() },
          onClose = { peopleViewModel.hideManageDialog() },
          managePeopleViewModel = hiltViewModel()
        )
      }
    }
  }

  if (uiState.showDeleteWarning) {
    AlertDialog(
      onDismissRequest = { peopleViewModel.hideDeleteWarning() },
      text = {
        Text(
          text = pluralStringResource(
            R.plurals.person_delete_warning,
            uiState.selected.size
          )
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            peopleViewModel.deleteSelected()
            peopleViewModel.hideDeleteWarning()
          }
        ) {
          Text(stringResource(R.string.action_delete))
        }
      },
      dismissButton = {
        TextButton(onClick = { peopleViewModel.hideDeleteWarning() }) {
          Text(stringResource(R.string.action_cancel))
        }
      }
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
              peopleViewModel.changeManageDialogMode(ManagePeopleMode.EDIT)
              peopleViewModel.showManageDialog()
            },
            onDeleteClick = { peopleViewModel.showDeleteWarning() },
            scrollBehavior = scrollBehavior
          )
        } else {
          SmallTopAppBar(
            navigationIcon = {
              IconButton(onClick = { navController.navigateUp() }) {
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
    },
    floatingActionButton = {
      AnimatedVisibility(
        visible = !selectionMode,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        ExtendedFloatingActionButton(
          onClick = { peopleViewModel.showManageDialog() },
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
        NoPeopleFound(modifier = Modifier.padding(innerPadding))
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
    }
  )
}

@Composable
fun SelectionTopAppBar(
  selectionCount: Int,
  onClearSelectionClick: () -> Unit = {},
  onEditClick: () -> Unit = {},
  onDeleteClick: () -> Unit = {},
  scrollBehavior: TopAppBarScrollBehavior
) {
  SmallTopAppBar(
    colors = TopAppBarDefaults.smallTopAppBarColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
      navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
      actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
      titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ),
    scrollBehavior = scrollBehavior,
    navigationIcon = {
      IconButton(onClick = onClearSelectionClick) {
        Icon(
          Icons.Default.Close,
          contentDescription = stringResource(R.string.action_cancel)
        )
      }
    },
    title = { Text(selectionCount.toString()) },
    actions = {
      if (selectionCount == 1) {
        IconButton(onClick = onEditClick) {
          Icon(
            Icons.Outlined.Edit,
            contentDescription = stringResource(R.string.action_edit)
          )
        }
      }

      IconButton(onClick = onDeleteClick) {
        Icon(
          Icons.Outlined.Delete,
          contentDescription = stringResource(R.string.action_delete)
        )
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

@Composable
fun NoPeopleFound(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = stringResource(R.string.no_people_found),
      textAlign = TextAlign.Center,
      style = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    )
  }
}