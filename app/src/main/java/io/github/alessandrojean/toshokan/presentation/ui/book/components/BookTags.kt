package io.github.alessandrojean.toshokan.presentation.ui.book.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explicit
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import io.github.alessandrojean.toshokan.database.data.Tag
import io.github.alessandrojean.toshokan.presentation.extensions.withTonalElevation

@Composable
fun BookTags(
  modifier: Modifier = Modifier,
  tags: List<Tag>,
  contentPadding: PaddingValues = PaddingValues(),
  expanded: Boolean = false,
  onTagClick: (Tag) -> Unit = {}
) {
  if (tags.isNotEmpty()) {
    Box(
      modifier = Modifier
        .animateContentSize()
        .then(modifier)
    ) {
      if (expanded) {
        FlowRow(
          modifier = Modifier
            .fillMaxWidth()
            .padding(contentPadding),
          mainAxisSpacing = 8.dp,
          crossAxisSpacing = 8.dp
        ) {
          tags.forEach { tag ->
            TagChip(
              tag = tag,
              onClick = { onTagClick(tag) }
            )
          }
        }
      } else {
        LazyRow(
          contentPadding = contentPadding,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          state = rememberLazyListState()
        ) {
          items(tags, key = { it.id }) { tag ->
            TagChip(
              tag = tag,
              onClick = { onTagClick(tag) }
            )
          }
        }
      }
    }
  }
}

@Composable
fun TagChip(
  modifier: Modifier = Modifier,
  tag: Tag,
  contentPadding: PaddingValues = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
  onClick: () -> Unit = {}
) {
  TagChip(
    modifier = modifier,
    name = tag.name,
    isNsfw = tag.is_nsfw,
    contentPadding = contentPadding,
    onClick = onClick
  )
}

@Composable
fun TagChip(
  modifier: Modifier = Modifier,
  name: String,
  isNsfw: Boolean = false,
  contentPadding: PaddingValues = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
  onClick: () -> Unit = {}
) {
  Surface(
    modifier = Modifier
      .clip(MaterialTheme.shapes.medium)
      .clickable { onClick() }
      .then(modifier),
    shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.surfaceVariant.withTonalElevation(2.dp),
  ) {
    Row(
      modifier = Modifier.padding(contentPadding),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      if (isNsfw) {
        Icon(
          painter = rememberVectorPainter(Icons.Outlined.Explicit),
          contentDescription = null,
          modifier = Modifier.size(FilterChipDefaults.IconSize)
        )
      }
      Text(
        modifier = Modifier.padding(contentPadding),
        text = name,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodyMedium
      )
    }
  }
}