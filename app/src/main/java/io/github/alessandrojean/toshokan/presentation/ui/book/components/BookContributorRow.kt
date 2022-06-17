package io.github.alessandrojean.toshokan.presentation.ui.book.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.database.data.BookContributor
import io.github.alessandrojean.toshokan.presentation.extensions.withTonalElevation

@Composable
fun BookContributorRow(
  modifier: Modifier = Modifier,
  contributor: BookContributor,
  onClick: (() -> Unit)? = null
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(
        enabled = onClick != null,
        onClick = onClick ?: {}
      )
      .padding(horizontal = 24.dp, vertical = 10.dp)
      .then(modifier),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      modifier = Modifier
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.surfaceVariant.withTonalElevation(6.dp))
        .padding(8.dp)
        .size(18.dp),
      imageVector = Icons.Outlined.Person,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Column(
      modifier = Modifier
        .padding(start = 16.dp)
        .weight(1f)
    ) {
      Text(
        modifier = Modifier.fillMaxWidth(),
        text = contributor.person_name,
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Text(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(contributor.role.title),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}
