package io.github.alessandrojean.toshokan.presentation.ui.book.manage.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.BookGroup
import io.github.alessandrojean.toshokan.database.data.Store
import io.github.alessandrojean.toshokan.presentation.ui.core.components.OutlinedDateField

@Composable
fun OrganizationTab(
  storeText: String,
  allStores: List<Store>,
  boughtAt: Long?,
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
  var storeExpanded by remember { mutableStateOf(false) }
  var groupExpanded by remember { mutableStateOf(false) }
  val focusManager = LocalFocusManager.current

  // TODO: Change to LazyColumn when the focus issue gets fixed.
  // Ref: https://issuetracker.google.com/issues/179203700
  // TODO: Handle focus on text fields when scroll.
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState),
    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top)
  ) {
    ExposedDropdownMenuBox(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp, start = 12.dp, end = 12.dp),
      expanded = storeExpanded,
      onExpandedChange = { storeExpanded = it }
    ) {
      val filteringOptions = allStores.filter { it.name.contains(storeText, true) }

      OutlinedTextField(
        modifier = Modifier
          .fillMaxWidth()
          .onFocusChanged { storeExpanded = it.isFocused },
        value = storeText,
        onValueChange = onStoreTextChange,
        singleLine = true,
        label = { Text(stringResource(R.string.store)) },
        isError = storeText.isEmpty(),
        trailingIcon = {
          if (filteringOptions.isNotEmpty()) {
            ExposedDropdownMenuDefaults.TrailingIcon(storeExpanded)
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

      if (filteringOptions.isNotEmpty()) {
        ExposedDropdownMenu(
          modifier = Modifier.exposedDropdownSize(),
          expanded = storeExpanded,
          onDismissRequest = { storeExpanded = false }
        ) {
          filteringOptions.forEach { selectionOption ->
            DropdownMenuItem(
              text = { Text(selectionOption.name) },
              onClick = {
                onStoreTextChange(selectionOption.name)
                onStoreChange(selectionOption)
                storeExpanded = false
              }
            )
          }
        }
      }
    }

    OutlinedDateField(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp),
      value = boughtAt,
      dialogTitle = stringResource(R.string.bought_at),
      label = { Text(stringResource(R.string.bought_at)) },
      onValueChange = onBoughtAtChange
    )

    ExposedDropdownMenuBox(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp),
      expanded = groupExpanded,
      onExpandedChange = { groupExpanded = it }
    ) {
      val filteringOptions = allGroups.filter { it.name.contains(groupText, true) }

      OutlinedTextField(
        modifier = Modifier
          .fillMaxWidth()
          .onFocusChanged { groupExpanded = it.isFocused },
        value = groupText,
        onValueChange = onGroupTextChange,
        singleLine = true,
        label = { Text(stringResource(R.string.group)) },
        isError = groupText.isEmpty(),
        trailingIcon = {
          if (filteringOptions.isNotEmpty()) {
            ExposedDropdownMenuDefaults.TrailingIcon(groupExpanded)
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

      if (filteringOptions.isNotEmpty()) {
        ExposedDropdownMenu(
          modifier = Modifier.exposedDropdownSize(),
          expanded = groupExpanded,
          onDismissRequest = { groupExpanded = false }
        ) {
          filteringOptions.forEach { selectionOption ->
            DropdownMenuItem(
              text = { Text(selectionOption.name) },
              onClick = {
                onGroupTextChange(selectionOption.name)
                onGroupChange(selectionOption)
                groupExpanded = false
              }
            )
          }
        }
      }
    }

    OutlinedTextField(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp),
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