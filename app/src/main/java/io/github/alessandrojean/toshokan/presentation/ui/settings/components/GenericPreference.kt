package io.github.alessandrojean.toshokan.presentation.ui.settings.components

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GenericPreference(
  modifier: Modifier = Modifier,
  title: String,
  summary: String? = null,
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
      .padding(horizontal = 16.dp, vertical = 12.dp)
      .then(modifier)
  ) {
    Text(text = title)

    if (summary.orEmpty().isNotEmpty()) {
      Text(
        text = summary!!,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}