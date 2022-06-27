package io.github.alessandrojean.toshokan.presentation.ui.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.alessandrojean.toshokan.presentation.ui.core.dialog.ItemPickerDialog

@Composable
fun <T> ListPreference(
  modifier: Modifier = Modifier,
  title: String,
  summary: String? = null,
  dialogTitle: String = title,
  enabled: Boolean = true,
  selected: T,
  options: List<T>,
  optionKey: (T) -> Any,
  optionText: (T) -> String = { it.toString() },
  onSelectedChange: (T) -> Unit
) {
  var showOptionsDialog by remember { mutableStateOf(false) }

  ItemPickerDialog(
    visible = showOptionsDialog,
    title = dialogTitle,
    selected = selected,
    items = options,
    itemKey = optionKey,
    itemText = optionText,
    onChoose = { onSelectedChange(it) },
    onDismiss = { showOptionsDialog = false }
  )

  GenericPreference(
    modifier = modifier,
    title = title,
    summary = summary,
    enabled = enabled,
    onClick = { showOptionsDialog = true }
  )
}