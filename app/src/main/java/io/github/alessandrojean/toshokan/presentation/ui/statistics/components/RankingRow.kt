package io.github.alessandrojean.toshokan.presentation.ui.statistics.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextOverflow
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.domain.RankingItem
import io.github.alessandrojean.toshokan.util.extension.toLocaleString
import io.github.alessandrojean.toshokan.util.extension.toOrdinal

@Composable
fun RankingRow(
  modifier: Modifier = Modifier,
  position: Int,
  item: RankingItem,
  onClick: (() -> Unit)? = null
) {
  RankingRow(
    modifier = modifier,
    position = position,
    title = item.title,
    count = item.count.toInt(),
    onClick = onClick
  )
}

@Composable
fun RankingRow(
  modifier: Modifier = Modifier,
  position: Int,
  title: String,
  count: Int,
  onClick: (() -> Unit)? = null
) {
  ListItem(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(
        enabled = onClick != null,
        onClick = onClick ?: {}
      )
      .then(modifier),
    trailingContent = {
      if (position in 1..3) {
        Icon(
          painter = painterResource(getRankingIcon(position)),
          contentDescription = null
        )
      } else {
        Text(
          text = remember(position) { position.toOrdinal() },
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    },
    headlineText = {
      Text(
        text = title,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    },
    supportingText = {
      Text(
        text = pluralStringResource(R.plurals.book_count, count, count.toLocaleString()),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  )
}

private fun getRankingIcon(position: Int): Int {
  return when (position) {
    1 -> R.drawable.ic_podium_gold
    2 -> R.drawable.ic_podium_silver
    else -> R.drawable.ic_podium_bronze
  }
}