package io.github.alessandrojean.toshokan.presentation.ui.createbook.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import io.github.alessandrojean.toshokan.service.lookup.LookupBookResult
import io.github.alessandrojean.toshokan.service.lookup.Provider
import io.github.alessandrojean.toshokan.util.toAmazonCoverUrl

private fun mapResultsToPresentation(results: Map<Provider, List<LookupBookResult>>):
  List<LookupBookResult> {
  return results.values.flatten()
}

@Composable
fun CreateBookResultList(
  results: Map<Provider, List<LookupBookResult>>,
  selected: LookupBookResult?,
  modifier: Modifier = Modifier,
  onResultClick: (LookupBookResult) -> Unit
) {
  val resultsMapped = mapResultsToPresentation(results)

  LazyColumn(
    modifier.selectableGroup(),
    verticalArrangement = Arrangement
      .spacedBy(8.dp, Alignment.Top),
    horizontalAlignment = Alignment.Start
  ) {
    items(resultsMapped) { result ->
      CreateBookResultRow(
        result = result,
        selected = result.hashCode() == selected?.hashCode(),
        onSelect = onResultClick
      )
    }
  }
}

@Composable
fun CreateBookResultRow(
  result: LookupBookResult,
  selected: Boolean,
  onSelect: (LookupBookResult) -> Unit
) {
  OutlinedCard(
    modifier = Modifier
      .fillMaxWidth()
      .selectable(
        selected = selected,
        onClick = { onSelect(result) },
        role = Role.RadioButton
      ),
    colors = CardDefaults.outlinedCardColors(
      containerColor = if (selected) {
        MaterialTheme.colorScheme.surfaceVariant
      } else {
        MaterialTheme.colorScheme.surface
      }
    )
  ) {
    Row(modifier = Modifier.fillMaxSize()) {
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
          .clip(MaterialTheme.shapes.medium)
          .background(MaterialTheme.colorScheme.background)
      )

      Column(
        modifier = Modifier
          .padding(8.dp)
          .fillMaxHeight()
      ) {
        Text(
          text = result.title,
          style = MaterialTheme.typography.titleMedium
        )
        Text(
          text = result.authors.joinToString(),
          style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.weight(1f))
        Box {
          Text(
            text = "${result.provider!!.title} · ${result.publisher}",
            style = MaterialTheme.typography.bodySmall
          )
        }
      }
    }
  }
}