package io.github.alessandrojean.toshokan.presentation.ui.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
  trailingContent: @Composable (() -> Unit)? = null,
  onCheckedChange: (Boolean) -> Unit = {}
) {
  ListItem(
    modifier = Modifier
      .fillMaxWidth()
      .toggleable(
        enabled = enabled,
        value = checked,
        onValueChange = { onCheckedChange.invoke(it) }
      )
      .then(modifier),
    headlineText = { Text(text = title) },
    supportingText = if (!summary.isNullOrBlank()) {
      { Text(text = summary) }
    } else null,
    trailingContent = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        trailingContent?.invoke()

        Switch(
          modifier = Modifier.padding(start = if (trailingContent != null) 16.dp else 8.dp),
          checked = checked,
          enabled = enabled,
          thumbContent = if (checked) {
            {
              Icon(
                imageVector = if (checked) Icons.Filled.Check else Icons.Outlined.Close,
                contentDescription = null,
                modifier = Modifier.size(SwitchDefaults.IconSize)
              )
            }
          } else null,
          onCheckedChange = null,
        )
      }

    }
  )
}