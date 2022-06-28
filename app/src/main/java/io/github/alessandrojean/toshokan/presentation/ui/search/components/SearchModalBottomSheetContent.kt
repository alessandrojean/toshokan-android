package io.github.alessandrojean.toshokan.presentation.ui.search.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.domain.SortColumn
import io.github.alessandrojean.toshokan.domain.SortDirection
import io.github.alessandrojean.toshokan.presentation.ui.core.components.ModalBottomSheetItem

@Composable
fun SortModalBottomSheetContent(
  modifier: Modifier = Modifier,
  columns: List<SortColumn> = remember { SortColumn.values().toList() },
  column: SortColumn,
  direction: SortDirection,
  onColumnChange: (SortColumn) -> Unit,
  onDirectionChange: (SortDirection) -> Unit,
) {
  val iconRotation by animateFloatAsState(if (direction == SortDirection.DESCENDING) 0f else 180f)

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .then(modifier)
  ) {
    Text(
      modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 16.dp),
      text = stringResource(R.string.action_sort_by),
      style = MaterialTheme.typography.titleMedium.copy(
        fontWeight = FontWeight.SemiBold
      )
    )

    columns.forEach { sortable ->
      ModalBottomSheetItem(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(sortable.title),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
        textPadding = PaddingValues(start = 18.dp),
        leadingIcon = {
          Box(modifier = Modifier.size(24.dp)) {
            androidx.compose.animation.AnimatedVisibility(
              visible = column == sortable,
              enter = fadeIn(),
              exit = fadeOut()
            ) {
              Icon(
                modifier = Modifier.graphicsLayer { rotationZ = iconRotation },
                painter = rememberVectorPainter(Icons.Outlined.ArrowUpward),
                tint = MaterialTheme.colorScheme.surfaceTint,
                contentDescription = null
              )
            }
          }
        },
        onClick = {
          if (column == sortable) {
            onDirectionChange(!direction)
          } else {
            onColumnChange(sortable)
          }
        }
      )
    }
  }
}