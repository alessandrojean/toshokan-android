package io.github.alessandrojean.toshokan.presentation.ui.core.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Deselect
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.extensions.surfaceWithTonalElevation
import io.github.alessandrojean.toshokan.presentation.ui.core.components.NoItemsFound
import io.github.alessandrojean.toshokan.presentation.ui.theme.DividerOpacity

@Composable
fun <T> FullScreenItemPickerDialog(
  visible: Boolean,
  title: String,
  selected: T? = null,
  items: List<T> = emptyList(),
  initialSearch: String = "",
  tonalElevation: Dp = 0.dp,
  itemKey: (T) -> Any,
  itemText: (T) -> String = { it.toString() },
  searchPlaceholder: String = stringResource(R.string.search_tip),
  onChoose: (T) -> Unit,
  dismissText: String = stringResource(R.string.action_cancel),
  onDismiss: () -> Unit,
  nullable: Boolean = false,
  onClear: () -> Unit = {},
  search: (String, List<T>) -> List<T> = { query, list ->
    list.filter { itemText.invoke(it).contains(query, ignoreCase = true) }
  },
  itemTrailingIcon: @Composable (T) -> Unit = {}
) {
  FullScreenItemPickerDialog(
    visible = visible,
    title = title,
    selected = if (selected == null) emptyList() else listOf(selected),
    items = items,
    initialSearch = initialSearch,
    tonalElevation = tonalElevation,
    itemKey = itemKey,
    itemText = itemText,
    searchPlaceholder = searchPlaceholder,
    onChoose = { selection -> onChoose(selection.first()) },
    dismissText = dismissText,
    onDismiss = onDismiss,
    nullable = nullable,
    onClear = onClear,
    search = search,
    itemTrailingIcon = itemTrailingIcon
  )
}

@Composable
fun <T> FullScreenItemPickerDialog(
  visible: Boolean,
  title: String,
  selected: List<T> = emptyList(),
  items: List<T> = emptyList(),
  initialSearch: String = "",
  tonalElevation: Dp = 0.dp,
  itemKey: (T) -> Any,
  itemText: (T) -> String = { it.toString() },
  searchPlaceholder: String = stringResource(R.string.search_tip),
  onChoose: (List<T>) -> Unit,
  dismissText: String = stringResource(R.string.action_cancel),
  onDismiss: () -> Unit,
  nullable: Boolean = false,
  onClear: () -> Unit = {},
  search: (String, List<T>) -> List<T> = { query, list ->
    list.filter { itemText.invoke(it).contains(query, ignoreCase = true) }
  },
  itemTrailingIcon: @Composable (T) -> Unit = {},
  role: Role = Role.RadioButton
) {
  if (visible) {
    val listState = rememberLazyListState(
      initialFirstVisibleItemIndex = if (selected.isNotEmpty()) {
        items.indexOf(selected.first())
      } else {
        0
      }
    )
    val selectedState = remember { selected.toMutableStateList() }
    var searchText by remember { mutableStateOf(initialSearch) }

    val filteredItems by remember(searchText) {
      derivedStateOf {
        if (searchText.isBlank()) {
          items
        } else {
          search.invoke(searchText, items)
        }
      }
    }

    LaunchedEffect(filteredItems) {
      listState.scrollToItem(0)
    }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    Dialog(
      onDismissRequest = onDismiss,
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
//      val keyboardController = LocalSoftwareKeyboardController.current
      val focusManager = LocalFocusManager.current

      Scaffold(
        modifier = Modifier.heightIn(min = screenHeight),
        containerColor = MaterialTheme.colorScheme.surfaceWithTonalElevation(tonalElevation),
        topBar = {
          Column(modifier = Modifier.fillMaxWidth()) {
            SmallTopAppBar(
              navigationIcon = {
                IconButton(onClick = onDismiss) {
                  Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = dismissText
                  )
                }
              },
              title = { Text(title) },
              actions = {
                if (nullable && selectedState.isNotEmpty()) {
                  IconButton(
                    onClick = {
                      if (role == Role.RadioButton) {
                        onClear()
                        onDismiss()
                      } else if (role == Role.Checkbox) {
                        selectedState.clear()
                        searchText = ""
                      }
                    }
                  ) {
                    Icon(
                      imageVector = Icons.Outlined.Deselect,
                      contentDescription = stringResource(R.string.action_clear)
                    )
                  }
                }
              }
            )
            OutlinedTextField(
              modifier = Modifier.fillMaxWidth(),
              value = searchText,
              onValueChange = { searchText = it },
              placeholder = {
                Text(
                  text = searchPlaceholder,
                  style = MaterialTheme.typography.bodyLarge
                )
              },
              trailingIcon = {
                AnimatedVisibility(
                  visible = searchText.isNotEmpty(),
                  enter = fadeIn(),
                  exit = fadeOut()
                ) {
                  IconButton(onClick = { searchText = "" }) {
                    Icon(
                      imageVector = Icons.Outlined.Cancel,
                      contentDescription = stringResource(R.string.action_clear)
                    )
                  }
                }
              },
              colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                containerColor = Color.Transparent,
                cursorColor = LocalContentColor.current
              ),
              textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = LocalContentColor.current
              ),
              maxLines = 1,
              singleLine = true,
              keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
              keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
              )
            )
            Divider(
              modifier = Modifier.fillMaxWidth(),
              color = LocalContentColor.current.copy(alpha = DividerOpacity)
            )
          }
        },
        floatingActionButton = {
          if (role == Role.Checkbox) {
            FloatingActionButton(
              onClick = {
                onChoose(selectedState)
                onDismiss()
            }) {
              Icon(
                imageVector = Icons.Outlined.Done,
                contentDescription = stringResource(R.string.action_finish)
              )
            }
          }
        },
        content = { innerPadding ->
          Crossfade(
            targetState = filteredItems.isEmpty(),
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          ) { isEmpty ->
            if (isEmpty) {
              NoItemsFound(
                modifier = Modifier.fillMaxSize(),
                text = stringResource(R.string.no_results_found),
                icon = Icons.Outlined.SearchOff
              )
            } else {
              LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
              ) {
                items(filteredItems, key = itemKey) { itemOption ->
                  ItemOption(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    text = itemText(itemOption),
                    role = role,
                    selected = itemOption in selectedState,
                    trailingContent = { itemTrailingIcon.invoke(itemOption) },
                    onClick = {
                      if (role == Role.RadioButton) {
                        selectedState.clear()
                        selectedState.add(itemOption)
                        onChoose(selectedState)
                        onDismiss()
                      } else if (role == Role.Checkbox) {
                        if (itemOption in selectedState) {
                          selectedState.remove(itemOption)
                        } else {
                          selectedState.add(itemOption)
                        }
                      }
                    }
                  )
                }
              }
            }
          }
        }
      )
    }
  }
}