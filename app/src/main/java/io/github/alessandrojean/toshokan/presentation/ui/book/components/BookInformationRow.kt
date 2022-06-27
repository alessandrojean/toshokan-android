package io.github.alessandrojean.toshokan.presentation.ui.book.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.alessandrojean.toshokan.util.extension.placeholder

@Composable
fun BookInformationRow(
  modifier: Modifier = Modifier,
  label: String,
  value: String
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      modifier = Modifier.placeholder(value.isEmpty()),
      text = label,
      style = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    )
    Text(
      modifier = Modifier.placeholder(value.isEmpty()),
      text = value.ifEmpty { "Information" },
      style = MaterialTheme.typography.bodyMedium
    )
  }
}