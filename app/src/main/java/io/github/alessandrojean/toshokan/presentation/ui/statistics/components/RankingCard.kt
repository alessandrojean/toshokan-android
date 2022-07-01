package io.github.alessandrojean.toshokan.presentation.ui.statistics.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RankingCard(
  modifier: Modifier = Modifier,
  title: String,
  icon: Painter,
  containerColor: Color = MaterialTheme.colorScheme.surface,
  tonalElevation: Dp = 2.dp,
  shape: Shape = MaterialTheme.shapes.large,
  contentPadding: PaddingValues = PaddingValues(vertical = 16.dp, horizontal = 8.dp),
  onClick: (() -> Unit)? = null
) {
  Surface(
    modifier = Modifier
      .clip(shape)
      .clickable(
        enabled = onClick != null,
        onClick = onClick ?: {},
        role = Role.Button
      )
      .then(modifier),
    color = containerColor,
    tonalElevation = tonalElevation,
    shape = shape
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(contentPadding),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        painter = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.secondary
      )

      Text(
        modifier = Modifier.padding(top = 12.dp),
        text = title,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodySmall.copy(
          fontWeight = FontWeight.Medium
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}