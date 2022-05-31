package io.github.alessandrojean.toshokan.presentation.ui.core.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.extensions.surfaceWithTonalElevation
import io.github.alessandrojean.toshokan.presentation.ui.core.components.NoItemsFound
import io.github.alessandrojean.toshokan.presentation.ui.theme.DividerOpacity
import io.github.alessandrojean.toshokan.presentation.ui.theme.ModalBottomSheetShape

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
  if (visible) {
    val listState = rememberLazyListState(
      initialFirstVisibleItemIndex = if (selected != null) {
        items.indexOf(selected)
      } else {
        0
      }
    )
    var selectedState by remember { mutableStateOf(selected) }
    var searchText by remember { mutableStateOf(initialSearch) }
    val keyboardController = LocalSoftwareKeyboardController.current

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

    Dialog(
      onDismissRequest = onDismiss,
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      Scaffold(
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
                if (nullable) {
                  TextButton(
                    onClick = {
                      onClear()
                      onDismiss()
                    }
                  ) {
                    Text(stringResource(R.string.action_clear))
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
                onDone = { keyboardController?.hide() }
              )
            )
            Divider(
              modifier = Modifier.fillMaxWidth(),
              color = LocalContentColor.current.copy(alpha = DividerOpacity)
            )
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
                    role = Role.RadioButton,
                    selected = selectedState == itemOption,
                    trailingIcon = { itemTrailingIcon.invoke(itemOption) },
                    onClick = {
                      selectedState = itemOption
                      onChoose(itemOption)
                      onDismiss()
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