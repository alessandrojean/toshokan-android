package io.github.alessandrojean.toshokan.presentation.ui.book.manage.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ManageSearch
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.BookGroup
import io.github.alessandrojean.toshokan.database.data.Store
import io.github.alessandrojean.toshokan.presentation.ui.core.components.OutlinedDateField
import io.github.alessandrojean.toshokan.presentation.ui.core.dialog.FullScreenItemPickerDialog
import io.github.alessandrojean.toshokan.util.extension.bringIntoViewOnFocus

@Composable
fun OrganizationTab(
  store: Store?,
  storeText: String,
  allStores: List<Store>,
  boughtAt: Long?,
  group: BookGroup?,
  groupText: String,
  allGroups: List<BookGroup>,
  notes: String,
  isFuture: Boolean,
  onStoreTextChange: (String) -> Unit,
  onStoreChange: (Store) -> Unit,
  onBoughtAtChange: (Long?) -> Unit,
  onGroupTextChange: (String) -> Unit,
  onGroupChange: (BookGroup) -> Unit,
  onNotesChange: (String) -> Unit,
  onIsFutureChange: (Boolean) -> Unit
) {
  val scrollState = rememberScrollState()
  var showStorePickerDialog by remember { mutableStateOf(false) }
  var showGroupPickerDialog by remember { mutableStateOf(false) }
  val focusManager = LocalFocusManager.current
  val scope = rememberCoroutineScope()

  FullScreenItemPickerDialog(
    visible = showGroupPickerDialog,
    title = stringResource(R.string.groups),
    selected = group,
    initialSearch = groupText,
    items = allGroups,
    itemKey = { it.id },
    itemText = { it.name },
    onChoose = {
      onGroupChange.invoke(it)
      onGroupTextChange.invoke(it.name)
    },
    onDismiss = { showGroupPickerDialog = false }
  )

  FullScreenItemPickerDialog(
    visible = showStorePickerDialog,
    title = stringResource(R.string.stores),
    selected = store,
    initialSearch = storeText,
    items = allStores,
    itemKey = { it.id },
    itemText = { it.name },
    onChoose = {
      onStoreChange.invoke(it)
      onStoreTextChange.invoke(it.name)
    },
    onDismiss = { showStorePickerDialog = false }
  )

  // TODO: Change to LazyColumn when the focus issue gets fixed.
  // Ref: https://issuetracker.google.com/issues/179203700
  // TODO: Handle focus on text fields when scroll.
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .navigationBarsPadding()
      .imePadding(),
    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top)
  ) {
    OutlinedTextField(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 12.dp, start = 12.dp, end = 12.dp)
        .bringIntoViewOnFocus(scope),
      value = storeText,
      onValueChange = onStoreTextChange,
      singleLine = true,
      label = { Text(stringResource(R.string.store)) },
      isError = storeText.isEmpty(),
      trailingIcon = {
        IconButton(onClick = { showStorePickerDialog = true }) {
          Icon(
            imageVector = Icons.Outlined.ManageSearch,
            contentDescription = stringResource(R.string.action_search)
          )
        }
      },
      colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
      keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
      keyboardActions = KeyboardActions(
        onNext = {
          focusManager.moveFocus(FocusDirection.Down)
        }
      )
    )

    OutlinedDateField(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp),
      value = boughtAt,
      dialogTitle = stringResource(R.string.bought_at),
      label = { Text(stringResource(R.string.bought_at)) },
      onValueChange = onBoughtAtChange
    )

    OutlinedTextField(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp)
        .bringIntoViewOnFocus(scope),
      value = groupText,
      onValueChange = onGroupTextChange,
      singleLine = true,
      label = { Text(stringResource(R.string.group)) },
      isError = groupText.isEmpty(),
      trailingIcon = {
        IconButton(onClick = { showGroupPickerDialog = true }) {
          Icon(
            imageVector = Icons.Outlined.ManageSearch,
            contentDescription = stringResource(R.string.action_search)
          )
        }
      },
      colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
      keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
      keyboardActions = KeyboardActions(
        onNext = {
          focusManager.moveFocus(FocusDirection.Down)
        }
      )
    )

    OutlinedTextField(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp)
        .bringIntoViewOnFocus(scope),
      value = notes,
      onValueChange = onNotesChange,
      maxLines = 10,
      label = { Text(stringResource(R.string.notes)) }
    )

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .toggleable(
          role = Role.Checkbox,
          value = isFuture,
          onValueChange = onIsFutureChange
        )
        .padding(horizontal = 12.dp, vertical = 16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Checkbox(
        checked = isFuture,
        onCheckedChange = null
      )
      Text(
        modifier = Modifier.padding(start = 16.dp),
        text = stringResource(R.string.future_item),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}