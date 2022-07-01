package io.github.alessandrojean.toshokan.presentation.ui.core.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.ui.theme.DividerOpacity
import io.github.alessandrojean.toshokan.util.extension.bottom
import io.github.alessandrojean.toshokan.util.extension.end
import io.github.alessandrojean.toshokan.util.extension.rememberScrollContext
import io.github.alessandrojean.toshokan.util.extension.start
import io.github.alessandrojean.toshokan.util.extension.top

@Composable
fun <T> ItemPickerDialog(
  visible: Boolean,
  title: String,
  selected: T? = null,
  items: List<T> = emptyList(),
  itemKey: (T) -> Any,
  itemText: (T) -> String = { it.toString() },
  dismissText: String = stringResource(R.string.action_cancel),
  onChoose: (T) -> Unit,
  onDismiss: () -> Unit
) {
  ItemPickerDialog(
    visible = visible,
    title = title,
    selected = listOfNotNull(selected),
    items = items,
    itemKey = itemKey,
    itemText = itemText,
    role = Role.RadioButton,
    chooseText = stringResource(R.string.action_select),
    onChoose = { onChoose(it.first()) },
    dismissText = dismissText,
    onDismiss = onDismiss
  )
}

@Composable
fun <T> ItemPickerDialog(
  visible: Boolean,
  title: String,
  selected: List<T> = emptyList(),
  items: List<T> = emptyList(),
  itemKey: (T) -> Any,
  itemText: (T) -> String = { it.toString() },
  chooseText: String = stringResource(R.string.action_select),
  onChoose: (List<T>) -> Unit,
  dismissText: String = stringResource(R.string.action_cancel),
  onDismiss: () -> Unit
) {
  ItemPickerDialog(
    visible = visible,
    title = title,
    selected = selected,
    items = items,
    itemKey = itemKey,
    itemText = itemText,
    role = Role.RadioButton,
    chooseText = chooseText,
    onChoose = onChoose,
    dismissText = dismissText,
    onDismiss = onDismiss
  )
}

@Composable
fun <T> ItemPickerDialog(
  visible: Boolean,
  title: String,
  selected: List<T> = emptyList(),
  items: List<T> = emptyList(),
  itemKey: (T) -> Any,
  itemText: (T) -> String = { it.toString() },
  role: Role = Role.Checkbox,
  chooseText: String = stringResource(R.string.action_select),
  onChoose: (List<T>) -> Unit,
  dismissText: String = stringResource(R.string.action_cancel),
  onDismiss: () -> Unit
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
    val scrollContext = rememberScrollContext(listState)

    EnhancedAlertDialog(
      onDismissRequest = onDismiss,
      title = {
        Text(
          text = title,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          AnimatedVisibility(!scrollContext.isAllItemsVisible && !scrollContext.isTop) {
            Divider(
              modifier = Modifier.fillMaxWidth(),
              color = LocalContentColor.current.copy(alpha = DividerOpacity)
            )
          }
          LazyColumn(
            state = listState,
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f, fill = false)
              .selectableGroup()
          ) {
            items(items, key = itemKey) { itemOption ->
              ItemOption(
                modifier = Modifier.fillMaxWidth(),
                text = itemText(itemOption),
                selected = selectedState.contains(itemOption),
                role = role,
                onClick = {
                  if (role == Role.Checkbox) {
                    selectedState += itemOption
                  } else if (role == Role.RadioButton) {
                    selectedState[0] = itemOption
                    onChoose(selectedState)
                    onDismiss()
                  }
                }
              )
            }
          }
          AnimatedVisibility(!scrollContext.isAllItemsVisible && !scrollContext.isBottom) {
            Divider(
              modifier = Modifier.fillMaxWidth(),
              color = LocalContentColor.current.copy(alpha = DividerOpacity)
            )
          }
        }
      },
      dismissButton = {
        TextButton(onClick = onDismiss) {
          Text(dismissText)
        }
      },
      confirmButton = {
        if (role == Role.Checkbox) {
          TextButton(
            enabled = selected.isNotEmpty(),
            onClick = {
              onChoose(selectedState.toList().filterNotNull())
              onDismiss()
            }
          ) {
            Text(chooseText)
          }
        }
      }
    )
  }
}

@Composable
fun ItemOption(
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp),
  text: String,
  role: Role,
  selected: Boolean,
  trailingContent: @Composable (() -> Unit)? = null,
  onClick: () -> Unit
) {
  val padding = PaddingValues(
    start = (contentPadding.start - 16.dp).coerceAtLeast(0.dp),
    end = (contentPadding.end - 16.dp).coerceAtLeast(0.dp),
    top = (contentPadding.top - 16.dp).coerceAtLeast(0.dp),
    bottom = (contentPadding.bottom - 16.dp).coerceAtLeast(0.dp)
  )

  ListItem(
    modifier = modifier
      .fillMaxWidth()
      .selectable(
        selected = selected,
        onClick = onClick,
        role = role
      )
      .padding(padding),
    leadingContent = {
      if (role == Role.RadioButton) {
        RadioButton(
          selected = selected,
          onClick = null
        )
      } else if (role == Role.Checkbox) {
        Checkbox(
          checked = selected,
          onCheckedChange = null
        )
      }
    },
    headlineText = {
      Text(
        modifier = Modifier.padding(start = padding.start),
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    },
    trailingContent = trailingContent
  )
}