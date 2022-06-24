package io.github.alessandrojean.toshokan.presentation.ui.core.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.alessandrojean.toshokan.R

@Composable
fun SelectionTopAppBar(
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = WindowInsets.statusBars.asPaddingValues(),
  selectionCount: Int,
  onClearSelectionClick: () -> Unit = {},
  onEditClick: (() -> Unit)? = null,
  onDeleteClick: () -> Unit = {},
  colors: TopAppBarColors = TopAppBarDefaults.smallTopAppBarColors(
    containerColor = MaterialTheme.colorScheme.surfaceVariant,
    scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant
  ),
  scrollBehavior: TopAppBarScrollBehavior? = null,
  content: @Composable ColumnScope.() -> Unit = {}
) {
  SelectionTopAppBar(
    modifier = modifier,
    contentPadding = contentPadding,
    colors = colors,
    scrollBehavior = scrollBehavior,
    selectionCount = selectionCount,
    onClearSelectionClick = onClearSelectionClick,
    actions = {
      if (selectionCount == 1 && onEditClick != null) {
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
    },
    content = content
  )
}

@Composable
fun SelectionTopAppBar(
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = WindowInsets.statusBars.asPaddingValues(),
  selectionCount: Int,
  onClearSelectionClick: () -> Unit = {},
  actions: @Composable RowScope.() -> Unit = {},
  colors: TopAppBarColors = TopAppBarDefaults.smallTopAppBarColors(
    containerColor = MaterialTheme.colorScheme.surfaceVariant,
    scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant
  ),
  scrollBehavior: TopAppBarScrollBehavior? = null,
  content: @Composable ColumnScope.() -> Unit = {}
) {
  EnhancedSmallTopAppBar(
    modifier = modifier,
    contentPadding = contentPadding,
    colors = colors,
    scrollBehavior = scrollBehavior,
    navigationIcon = {
      IconButton(onClick = onClearSelectionClick) {
        Icon(
          Icons.Default.Close,
          contentDescription = stringResource(R.string.action_cancel)
        )
      }
    },
    title = {
      AnimatedContent(targetState = selectionCount) { targetCount ->
        Text(targetCount.toString())
      }
    },
    actions = actions,
    content = content
  )
}
