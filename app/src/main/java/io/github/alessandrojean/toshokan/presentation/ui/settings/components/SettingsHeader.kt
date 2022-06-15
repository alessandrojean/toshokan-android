package io.github.alessandrojean.toshokan.presentation.ui.settings.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsHeader(
  modifier: Modifier = Modifier,
  title: String
) {
  Text(
    modifier = Modifier
      .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 16.dp)
      .then(modifier),
    color = MaterialTheme.colorScheme.secondary,
    style = MaterialTheme.typography.bodyMedium,
    text = title
  )
}