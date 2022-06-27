package io.github.alessandrojean.toshokan.presentation.ui.book.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.util.extension.placeholder

@Composable
fun BookMetadataRow(
  modifier: Modifier = Modifier,
  label: String,
  value: String,
  enabled: Boolean = true,
  onClick: () -> Unit = {}
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(
        enabled = enabled,
        onClick = onClick
      )
      .padding(horizontal = 24.dp, vertical = 10.dp)
      .then(modifier)
  ) {
    Text(
      modifier = Modifier.placeholder(value.isEmpty()),
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
    Text(
      modifier = Modifier.placeholder(value.isEmpty()),
      text = value.ifEmpty { "Metadata value" },
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}