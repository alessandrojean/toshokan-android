package io.github.alessandrojean.toshokan.presentation.ui.stores

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
import io.github.alessandrojean.toshokan.database.data.Store
import io.github.alessandrojean.toshokan.presentation.ui.core.components.NoItemsFound
import io.github.alessandrojean.toshokan.presentation.ui.core.components.SelectionTopAppBar
import io.github.alessandrojean.toshokan.presentation.ui.stores.manage.ManageStoreMode
import io.github.alessandrojean.toshokan.presentation.ui.stores.manage.ManageStoreDialog

@Composable
fun StoresScreen(
  navController: NavController,
  storesViewModel: StoresViewModel
) {
  val uiState by storesViewModel.uiState.collectAsState()
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

  val stores by uiState.stores.collectAsState(emptyList())

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

  if (uiState.showManageDialog) {
    ManageStoreDialog(
      mode = uiState.manageDialogMode,
      store = stores.firstOrNull { it.id == uiState.selected.firstOrNull() },
      onClose = { storesViewModel.hideManageDialog() },
      manageStoreViewModel = hiltViewModel()
    )
  } else if (uiState.showDeleteWarning) {
    DeleteStoresWarningDialog(
      selectedCount = uiState.selected.size,
      onClose = { storesViewModel.hideDeleteWarning() },
      onConfirmClick = {
        storesViewModel.deleteSelected()
        storesViewModel.hideDeleteWarning()
      },
      onDismissClick = { storesViewModel.hideDeleteWarning() }
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
            onClearSelectionClick = { storesViewModel.clearSelection() },
            onEditClick = {
              storesViewModel.changeManageDialogMode(ManageStoreMode.EDIT)
              storesViewModel.showManageDialog()
            },
            onDeleteClick = { storesViewModel.showDeleteWarning() },
            scrollBehavior = scrollBehavior
          )
        } else {
          SmallTopAppBar(
            modifier = Modifier.statusBarsPadding(),
            navigationIcon = {
              IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                  Icons.Default.ArrowBack,
                  contentDescription = stringResource(R.string.action_back)
                )
              }
            },
            title = { Text(stringResource(R.string.stores)) },
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
          onClick = { storesViewModel.showManageDialog() },
          expanded = expandedFab,
          icon = {
            Icon(
              imageVector = Icons.Filled.Add,
              contentDescription = stringResource(R.string.create_store)
            )
          },
          text = { Text(stringResource(R.string.create_store)) }
        )
      }
    },
    content = { innerPadding ->
      if (stores.isEmpty()) {
        NoItemsFound(
          modifier = Modifier.padding(innerPadding),
          text = stringResource(R.string.no_stores_found)
        )
      } else {
        LazyColumn(
          contentPadding = innerPadding,
          modifier = Modifier.selectableGroup(),
          state = listState,
          verticalArrangement = Arrangement.Top,
          horizontalAlignment = Alignment.Start
        ) {
          items(stores) { store ->
            StoreItem(
              modifier = Modifier.fillMaxWidth(),
              store = store,
              selected = store.id in uiState.selected,
              onClick = {
                if (selectionMode) {
                  storesViewModel.toggleSelection(store.id)
                }
              },
              onLongClick = { storesViewModel.toggleSelection(store.id) }
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
fun DeleteStoresWarningDialog(
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
          R.plurals.store_delete_warning,
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
fun StoreItem(
  modifier: Modifier = Modifier,
  store: Store,
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
    Text(text = store.name)
  }
}
