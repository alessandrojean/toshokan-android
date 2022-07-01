package io.github.alessandrojean.toshokan.presentation.ui.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun SettingsCategory(
  modifier: Modifier = Modifier,
  icon: ImageVector,
  title: String,
  onClick: () -> Unit = {}
) {
  SettingsCategory(
    modifier = modifier,
    icon = rememberVectorPainter(icon),
    title = title,
    onClick = onClick
  )
}

@Composable
fun SettingsCategory(
  modifier: Modifier = Modifier,
  icon: Painter,
  title: String,
  onClick: () -> Unit = {}
) {
  ListItem(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .then(modifier),
    leadingContent = {
      Icon(
        painter = icon,
        contentDescription = title,
        tint = MaterialTheme.colorScheme.surfaceTint
      )
    },
    headlineText = {
      Text(
        text = title,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  )
}