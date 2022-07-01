package io.github.alessandrojean.toshokan.presentation.ui.statistics.components

import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.alessandrojean.toshokan.presentation.extensions.withTonalElevation

@Composable
fun StatisticCard(
  modifier: Modifier = Modifier,
  title: String,
  value: String,
  icon: ImageVector,
  showValue: Boolean = true,
  contentPadding: PaddingValues = PaddingValues(),
  onClick: (() -> Unit)? = null,
) {
  var valueSize by remember { mutableStateOf<IntSize?>(null) }
  val valueWidth = with(LocalDensity.current) { valueSize?.width?.toDp() }
  val valueHeight = with(LocalDensity.current) { valueSize?.height?.toDp() }

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .padding(contentPadding)
      .clip(MaterialTheme.shapes.large)
      .clickable(
        enabled = onClick != null,
        onClick = onClick ?: {}
      )
      .then(modifier),
    tonalElevation = 6.dp,
    shape = MaterialTheme.shapes.large
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(64.dp)
          .background(MaterialTheme.colorScheme.surfaceVariant.withTonalElevation(6.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Column(
        modifier = Modifier
          .padding(8.dp)
          .weight(1f)
      ) {
        Text(
          text = title,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(
          modifier = Modifier.padding(top = 2.dp),
          contentAlignment = Alignment.CenterStart
        ) {
          Text(
            modifier = Modifier.onSizeChanged { valueSize = it },
            text = value,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.surfaceTint,
            style = MaterialTheme.typography.bodyLarge.copy(
              fontWeight = FontWeight.SemiBold,
              fontSize = 20.sp
            )
          )

          androidx.compose.animation.AnimatedVisibility(
            visible = !showValue,
            enter = expandHorizontally(expandFrom = Alignment.Start),
            exit = shrinkHorizontally(shrinkTowards = Alignment.Start)
          ) {
            Box(
              modifier = Modifier
                .width(valueWidth ?: 0.dp)
                .height(valueHeight ?: 0.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(
                  MaterialTheme.colorScheme.surfaceVariant.withTonalElevation(2.dp)
                )
            )
          }
        }
      }
    }
  }
}