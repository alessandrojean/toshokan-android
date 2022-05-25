package io.github.alessandrojean.toshokan.presentation.ui.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


@Composable
fun NoItemsFound(
  modifier: Modifier = Modifier,
  text: String,
  icon: ImageVector? = null
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .then(modifier),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    if (icon != null) {
      Icon(
        imageVector = icon,
        contentDescription = text,
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
        modifier = Modifier
          .size(96.dp)
          .padding(bottom = 16.dp)
      )
    }

    Text(
      text = text,
      textAlign = TextAlign.Center,
      style = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    )
  }
}