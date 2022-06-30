package io.github.alessandrojean.toshokan.presentation.ui.tags

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Explicit
import androidx.compose.material.icons.outlined.Label
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
import io.github.alessandrojean.toshokan.database.data.Tag
import io.github.alessandrojean.toshokan.presentation.extensions.selection
import io.github.alessandrojean.toshokan.presentation.ui.core.components.EnhancedSmallTopAppBar
import io.github.alessandrojean.toshokan.presentation.ui.core.components.NoItemsFound
import io.github.alessandrojean.toshokan.presentation.ui.core.components.SelectionTopAppBar
import io.github.alessandrojean.toshokan.presentation.ui.tags.manage.ManageTagDialog
import io.github.alessandrojean.toshokan.presentation.ui.tags.manage.ManageTagMode
import io.github.alessandrojean.toshokan.util.extension.collectAsStateWithLifecycle
import io.github.alessandrojean.toshokan.util.extension.plus

class TagsScreen : AndroidScreen() {

  @Composable
  override fun Content() {
    val navigator = LocalNavigator.currentOrThrow
    val tagsViewModel = getViewModel<TagsViewModel>()
    val uiState by tagsViewModel.uiState.collectAsStateWithLifecycle()
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

    val tags by tagsViewModel.tags.collectAsStateWithLifecycle(emptyList())

    BackHandler(selectionMode) {
      tagsViewModel.clearSelection()
    }

    if (uiState.showManageDialog) {
      ManageTagDialog(
        mode = uiState.manageDialogMode,
        tag = tags.firstOrNull { it.id == uiState.selected.firstOrNull() },
        onClose = { tagsViewModel.hideManageDialog() },
        manageTagViewModel = getViewModel()
      )
    } else if (uiState.showDeleteWarning) {
      DeleteTagsWarningDialog(
        selectedCount = uiState.selected.size,
        onClose = { tagsViewModel.hideDeleteWarning() },
        onConfirmClick = {
          tagsViewModel.deleteSelected()
          tagsViewModel.hideDeleteWarning()
        },
        onDismissClick = { tagsViewModel.hideDeleteWarning() }
      )
    }

    val bottomPadding = WindowInsets.navigationBars.asPaddingValues()
    val fabPadding = PaddingValues(bottom = 76.dp)

    Scaffold(
      modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
      topBar = {
        Crossfade(targetState = selectionMode) { selection ->
          if (selection) {
            SelectionTopAppBar(
              selectionCount = uiState.selected.size,
              onClearSelectionClick = { tagsViewModel.clearSelection() },
              onEditClick = {
                tagsViewModel.changeManageDialogMode(ManageTagMode.EDIT)
                tagsViewModel.showManageDialog()
              },
              onDeleteClick = { tagsViewModel.showDeleteWarning() },
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
              title = { Text(stringResource(R.string.tags)) },
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
            modifier = Modifier.padding(bottomPadding),
            onClick = { tagsViewModel.showManageDialog() },
            expanded = expandedFab,
            icon = {
              Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.create_tag)
              )
            },
            text = { Text(stringResource(R.string.create_tag)) }
          )
        }
      },
      content = { innerPadding ->
        val padding = innerPadding + bottomPadding

        if (tags.isEmpty()) {
          NoItemsFound(
            modifier = Modifier.padding(padding),
            text = stringResource(R.string.no_tags_found),
            icon = Icons.Outlined.Label
          )
        } else {
          LazyColumn(
            contentPadding = padding + if (!selectionMode) fabPadding else PaddingValues(),
            modifier = Modifier.selectableGroup(),
            state = listState,
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
          ) {
            items(tags, key = { it.id }) { tag ->
              TagItem(
                modifier = Modifier.fillMaxWidth(),
                tag = tag,
                selected = tag.id in uiState.selected,
                onClick = {
                  if (selectionMode) {
                    tagsViewModel.toggleSelection(tag.id)
                  }
                },
                onLongClick = { tagsViewModel.toggleSelection(tag.id) }
              )
            }
          }
        }
      }
    )
  }

  @Composable
  fun DeleteTagsWarningDialog(
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
            R.plurals.tag_delete_warning,
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
  fun TagItem(
    modifier: Modifier = Modifier,
    tag: Tag,
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
      Icon(
        painter = if (tag.is_nsfw) {
          rememberVectorPainter(Icons.Outlined.Explicit)
        } else {
          rememberVectorPainter(Icons.Outlined.Label)
        },
        contentDescription = null,
        modifier = Modifier.padding(end = 24.dp)
      )
      Text(
        modifier = Modifier.weight(1f),
        text = tag.name,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }

}
