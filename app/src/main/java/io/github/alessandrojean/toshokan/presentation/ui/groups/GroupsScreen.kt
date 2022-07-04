package io.github.alessandrojean.toshokan.presentation.ui.groups

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material.icons.outlined.Reorder
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.BookGroup
import io.github.alessandrojean.toshokan.presentation.extensions.selection
import io.github.alessandrojean.toshokan.presentation.ui.core.components.EnhancedSmallTopAppBar
import io.github.alessandrojean.toshokan.presentation.ui.core.components.NoItemsFound
import io.github.alessandrojean.toshokan.presentation.ui.core.components.SelectionTopAppBar
import io.github.alessandrojean.toshokan.presentation.ui.groups.manage.ManageGroupDialog
import io.github.alessandrojean.toshokan.presentation.ui.groups.manage.ManageGroupMode
import io.github.alessandrojean.toshokan.util.extension.collectAsStateWithLifecycle
import io.github.alessandrojean.toshokan.util.extension.plus
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

class GroupsScreen : AndroidScreen() {

  @Composable
  override fun Content() {
    val navigator = LocalNavigator.currentOrThrow
    val groupsViewModel = getViewModel<GroupsViewModel>()
    val uiState by groupsViewModel.uiState.collectAsStateWithLifecycle()
    val topAppBarScrollState = rememberTopAppBarScrollState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior(topAppBarScrollState) }
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

    val groups by groupsViewModel.groups.collectAsStateWithLifecycle(emptyList())
    val reorderingItems = remember { mutableStateListOf<BookGroup>() }
    val reorderableState = rememberReorderableLazyListState(
      listState = listState,
      onMove = { from, to -> reorderingItems.add(to.index, reorderingItems.removeAt(from.index)) }
    )

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
        manageGroupViewModel = getViewModel()
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
            EnhancedSmallTopAppBar(
              contentPadding = WindowInsets.statusBars.asPaddingValues(),
              navigationIcon = {
                if (uiState.reorderMode) {
                  IconButton(
                    onClick = {
                      groupsViewModel.exitReorderMode()
                      reorderingItems.clear()
                    }
                  ) {
                    Icon(
                      Icons.Default.Close,
                      contentDescription = stringResource(R.string.action_cancel)
                    )
                  }
                } else {
                  IconButton(onClick = { navigator.pop() }) {
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
            modifier = Modifier
              .padding(innerPadding)
              .navigationBarsPadding(),
            text = stringResource(R.string.no_groups_found),
            icon = Icons.Outlined.GroupWork
          )
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
              .selectableGroup()
              .then(if (uiState.reorderMode) Modifier.reorderable(reorderableState) else Modifier),
            contentPadding = WindowInsets.navigationBars.asPaddingValues(),
            state = reorderableState.listState
          ) {
            items(
              items = if (uiState.reorderMode) reorderingItems else groups,
              key = { it.id }
            ) { group ->
              ReorderableItem(reorderableState, key = group.id) { isDragging ->
                GroupItem(
                  modifier = Modifier.fillMaxWidth(),
                  group = group,
                  selected = group.id in uiState.selected,
                  reorderMode = uiState.reorderMode,
                  reordering = isDragging,
                  reorderableState = reorderableState,
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
    reorderableState: ReorderableLazyListState,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
  ) {
    val backgroundColor by animateColorAsState(
      targetValue = if (selected || reordering) {
        MaterialTheme.colorScheme.selection
      } else {
        MaterialTheme.colorScheme.surface
      }
    )

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
        .background(backgroundColor)
        .padding(16.dp)
    ) {
      if (reorderMode) {
        Icon(
          painter = rememberVectorPainter(Icons.Outlined.DragIndicator),
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier
            .padding(end = 24.dp)
            .then(if (reorderMode) Modifier.detectReorder(reorderableState) else Modifier)
        )
      }
      Text(
        text = group.name,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }

}
