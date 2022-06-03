package io.github.alessandrojean.toshokan.presentation.ui.core.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun ExpandedIconButton(
  modifier: Modifier = Modifier,
  icon: ImageVector,
  text: String,
  onClick: () -> Unit,
  enabled: Boolean = true,
  contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
  TextButton(
    modifier = modifier,
    enabled = enabled,
    onClick = onClick,
    colors = ButtonDefaults.textButtonColors(
      contentColor = contentColor
    )
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Icon(
        modifier = Modifier.size(ButtonDefaults.IconSize),
        imageVector = icon,
        contentDescription = text
      )
      Text(
        modifier = Modifier.padding(top = 4.dp),
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}