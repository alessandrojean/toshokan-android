package io.github.alessandrojean.toshokan.presentation.ui.groups

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material.icons.outlined.Reorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.BookGroup
import io.github.alessandrojean.toshokan.presentation.ui.core.components.NoItemsFound
import io.github.alessandrojean.toshokan.presentation.ui.core.components.SelectionTopAppBar
import io.github.alessandrojean.toshokan.presentation.ui.groups.manage.ManageGroupMode
import io.github.alessandrojean.toshokan.presentation.ui.groups.manage.ManageGroupDialog
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.draggedItem
import org.burnoutcrew.reorderable.move
import org.burnoutcrew.reorderable.rememberReorderLazyListState
import org.burnoutcrew.reorderable.reorderable

@Composable
fun GroupsScreen(
  navController: NavController,
  groupsViewModel: GroupsViewModel
) {
  val uiState by groupsViewModel.uiState.collectAsState()
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

  val groups by uiState.groups.collectAsState(emptyList())
  val reorderingItems = remember { mutableStateListOf<BookGroup>() }
  val reorderableState = rememberReorderLazyListState(
    listState = listState,
    onMove = { from, to -> reorderingItems.move(from.index, to.index) }
  )

  val systemUiController = rememberSystemUiController()
  val statusBarColor = when {
    selectionMode -> MaterialTheme.colorScheme.surfaceVariant
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

  BackHandler(enabled = selectionMode || uiState.reorderMode) {
    if (selectionMode) {
      groupsViewModel.clearSelection()
    } else if (uiState.reorderMode) {
      groupsViewModel.exitReorderMode()
      reorderingItems.clear()
    }
  }

  if (uiState.showManageDialog) {
    ManageGroupDialog(
      mode = uiState.manageDialogMode,
      group = groups.firstOrNull { it.id == uiState.selected.firstOrNull() },
      onClose = { groupsViewModel.hideManageDialog() },
      manageGroupViewModel = hiltViewModel()
    )
  } else if (uiState.showDeleteWarning) {
    DeleteGroupsWarningDialog(
      selectedCount = uiState.selected.size,
      onClose = { groupsViewModel.hideDeleteWarning() },
      onConfirmClick = {
        groupsViewModel.deleteSelected()
        groupsViewModel.hideDeleteWarning()
      },
      onDismissClick = { groupsViewModel.hideDeleteWarning() }
    )
  }

  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      Crossfade(targetState = selectionMode) { selection ->
        if (selection) {
          SelectionTopAppBar(
            modifier = Modifier.statusBarsPadding(),
            selectionCount = uiState.selected.size,
            onClearSelectionClick = { groupsViewModel.clearSelection() },
            onEditClick = {
              groupsViewModel.changeManageDialogMode(ManageGroupMode.EDIT)
              groupsViewModel.showManageDialog()
            },
            onDeleteClick = { groupsViewModel.showDeleteWarning() },
            scrollBehavior = scrollBehavior
          )
        } else {
          SmallTopAppBar(
            modifier = Modifier.statusBarsPadding(),
            navigationIcon = {
              if (uiState.reorderMode) {
                IconButton(onClick = { groupsViewModel.exitReorderMode() }) {
                  Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.action_cancel)
                  )
                }
              } else {
                IconButton(onClick = { navController.navigateUp() }) {
                  Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.action_back)
                  )
                }
              }
            },
            title = { Text(stringResource(R.string.groups)) },
            actions = {
              if (groups.size > 1 && !uiState.reorderMode) {
                IconButton(
                  onClick = {
                    reorderingItems.addAll(groups)
                    groupsViewModel.enterReorderMode()
                  }
                ) {
                  Icon(
                    imageVector = Icons.Outlined.Reorder,
                    contentDescription = stringResource(R.string.action_reorder)
                  )
                }
              } else if (uiState.reorderMode) {
                IconButton(
                  onClick = {
                    groupsViewModel.reorderItems(reorderingItems.map(BookGroup::id))
                    groupsViewModel.exitReorderMode()
                    reorderingItems.clear()
                  }
                ) {
                  Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = stringResource(R.string.action_finish)
                  )
                }
              }
            },
            scrollBehavior = scrollBehavior
          )
        }
      }
    },
    floatingActionButton = {
      AnimatedVisibility(
        visible = !selectionMode && !uiState.reorderMode,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        ExtendedFloatingActionButton(
          onClick = { groupsViewModel.showManageDialog() },
          expanded = expandedFab,
          icon = {
            Icon(
              imageVector = Icons.Filled.Add,
              contentDescription = stringResource(R.string.create_group)
            )
          },
          text = { Text(stringResource(R.string.create_group)) }
        )
      }
    },
    content = { innerPadding ->
      if (groups.isEmpty()) {
        NoItemsFound(
          modifier = Modifier.padding(innerPadding),
          text = stringResource(R.string.no_groups_found),
          icon = Icons.Outlined.GroupWork
        )
      } else {
        LazyColumn(
          contentPadding = innerPadding,
          modifier = Modifier
            .selectableGroup()
            .let {
              if (uiState.reorderMode) {
                it.reorderable(reorderableState)
              } else {
                it
              }
           },
          state = if (uiState.reorderMode) reorderableState.listState else listState,
          verticalArrangement = Arrangement.Top,
          horizontalAlignment = Alignment.Start
        ) {
          itemsIndexed(
            items = if (uiState.reorderMode) reorderingItems else groups
          ) { idx, group ->
            GroupItem(
              modifier = Modifier
                .fillMaxWidth()
                .let { modifier ->
                  if (uiState.reorderMode) {
                    modifier
                      .draggedItem(reorderableState.offsetByIndex(idx))
                      .detectReorder(reorderableState)
                  } else {
                    modifier
                  }
                 },
              group = group,
              selected = group.id in uiState.selected,
              reorderMode = uiState.reorderMode,
              reordering = reorderableState.draggedIndex == idx,
              onClick = {
                if (selectionMode) {
                  groupsViewModel.toggleSelection(group.id)
                }
              },
              onLongClick = { groupsViewModel.toggleSelection(group.id) }
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
fun DeleteGroupsWarningDialog(
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
          R.plurals.group_delete_warning,
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
fun GroupItem(
  modifier: Modifier = Modifier,
  group: BookGroup,
  selected: Boolean = false,
  reordering: Boolean = false,
  reorderMode: Boolean = false,
  onClick: () -> Unit = {},
  onLongClick: () -> Unit = {}
) {
  Row(
    modifier = modifier
      .let { letModifier ->
        if (!reorderMode) {
          letModifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick,
            role = Role.Checkbox
          )
        } else {
          letModifier
        }
      }
      .background(
        color = if (selected || reordering) {
          MaterialTheme.colorScheme.surfaceVariant
        } else {
          MaterialTheme.colorScheme.surface
        }
      )
      .padding(16.dp)
  ) {
    if (reorderMode) {
      Icon(
        imageVector = Icons.Outlined.DragHandle,
        contentDescription = null,
        modifier = Modifier.padding(end = 24.dp)
      )
    }
    Text(text = group.name)
  }
}
