package io.github.alessandrojean.toshokan.presentation.ui.core.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.alessandrojean.toshokan.R

@Composable
fun SelectionTopAppBar(
  modifier: Modifier = Modifier,
  selectionCount: Int,
  onClearSelectionClick: () -> Unit = {},
  onEditClick: () -> Unit = {},
  onDeleteClick: () -> Unit = {},
  scrollBehavior: TopAppBarScrollBehavior
) {
  SmallTopAppBar(
    modifier = modifier,
    colors = TopAppBarDefaults.smallTopAppBarColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
      navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
      actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
      titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ),
    scrollBehavior = scrollBehavior,
    navigationIcon = {
      IconButton(onClick = onClearSelectionClick) {
        Icon(
          Icons.Default.Close,
          contentDescription = stringResource(R.string.action_cancel)
        )
      }
    },
    title = { Text(selectionCount.toString()) },
    actions = {
      if (selectionCount == 1) {
        IconButton(onClick = onEditClick) {
          Icon(
            Icons.Outlined.Edit,
            contentDescription = stringResource(R.string.action_edit)
          )
        }
      }

      IconButton(onClick = onDeleteClick) {
        Icon(
          Icons.Outlined.Delete,
          contentDescription = stringResource(R.string.action_delete)
        )
      }
    }
  )
}
