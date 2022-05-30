package io.github.alessandrojean.toshokan.presentation.ui.isbnlookup.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import io.github.alessandrojean.toshokan.domain.CreditRole
import io.github.alessandrojean.toshokan.service.lookup.LookupBookResult
import io.github.alessandrojean.toshokan.util.toAmazonCoverUrl

@Composable
fun IsbnLookupResultList(
  results: SnapshotStateList<LookupBookResult>,
  modifier: Modifier = Modifier,
  listState: LazyListState,
  contentPadding: PaddingValues,
  onResultClick: (LookupBookResult) -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .then(modifier),
    contentPadding = contentPadding,
    state = listState,
    verticalArrangement = Arrangement
      .spacedBy(8.dp, Alignment.Top),
    horizontalAlignment = Alignment.Start
  ) {
    items(results) { result ->
      IsbnLookupResultRow(
        result = result,
        onClick = onResultClick
      )
    }
  }
}

@Composable
fun IsbnLookupResultRow(
  result: LookupBookResult,
  onClick: (LookupBookResult) -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(MaterialTheme.shapes.large)
      .clickable { onClick(result) },
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(8.dp)
    ) {
      SubcomposeAsyncImage(
        model = result.coverUrl.ifEmpty {
          result.isbn.toAmazonCoverUrl()
        },
        contentDescription = result.title,
        contentScale = ContentScale.FillBounds,
        loading = {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Filled.Image,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(24.dp)
            )
          }
        },
        error = {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Filled.BrokenImage,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.size(24.dp)
            )
          }
        },
        modifier = Modifier
          .width(64.dp)
          .height(96.dp)
          .clip(MaterialTheme.shapes.large)
          .background(MaterialTheme.colorScheme.background)
      )

      Column(
        modifier = Modifier
          .padding(horizontal = 8.dp)
          .fillMaxHeight()
      ) {
        Text(
          text = result.title,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          style = MaterialTheme.typography.titleMedium
        )
        Text(
          text = result.contributors
            .filter { it.role == CreditRole.AUTHOR || it.role == CreditRole.ILLUSTRATOR }
            .joinToString { it.name },
          style = MaterialTheme.typography.bodyMedium,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = "${stringResource(result.provider!!.title)} · ${result.publisher}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
          modifier = Modifier.padding(top = 4.dp),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}