package io.github.alessandrojean.toshokan.presentation.ui.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SwitchPreference(
  modifier: Modifier = Modifier,
  title: String,
  summary: String? = null,
  checked: Boolean = false,
  enabled: Boolean = true,
  trailingContent: @Composable () -> Unit = {},
  onCheckedChange: (Boolean) -> Unit = {}
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .toggleable(
        enabled = enabled,
        value = checked,
        onValueChange = { onCheckedChange.invoke(it) }
      )
      .padding(16.dp)
      .then(modifier),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(text = title)

      if (summary.orEmpty().isNotEmpty()) {
        Text(
          text = summary!!,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    trailingContent.invoke()

    Switch(
      modifier = Modifier.padding(start = 16.dp),
      checked = checked,
      enabled = enabled,
      onCheckedChange = null,
    )
  }
}