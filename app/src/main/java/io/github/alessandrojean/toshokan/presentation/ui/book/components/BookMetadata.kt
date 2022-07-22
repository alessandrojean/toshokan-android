package io.github.alessandrojean.toshokan.presentation.ui.book.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.util.extension.placeholder
import io.github.alessandrojean.toshokan.util.extension.toLocaleString

@Composable
fun BookMetadata(
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  inLibrary: Boolean,
  publisher: String,
  hasBookNeighbors: Boolean,
  series: String? = null,
  language: String? = null,
  pageCount: Int? = null,
  group: String,
  dimensions: String,
  labelPrice: String,
  paidPrice: String,
  store: String,
  boughtAt: String? = null,
  latestReading: String? = null,
  onPublisherClick: () -> Unit,
  onSeriesClick: () -> Unit,
  onGroupClick: () -> Unit,
  onStoreClick: () -> Unit,
  onBoughtAtClick: () -> Unit,
  onLatestReadingClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .then(modifier)
  ) {
    Text(
      modifier = Modifier
        .padding(
          start = 24.dp,
          end = 24.dp,
          top = 32.dp,
          bottom = 12.dp
        )
        .semantics { heading() },
      text = stringResource(R.string.metadata),
      style = MaterialTheme.typography.titleLarge
    )
    BookMetadataRow(
      label = stringResource(R.string.publisher),
      value = publisher,
      enabled = enabled,
      onClick = onPublisherClick.takeIf { inLibrary }
    )
    if (hasBookNeighbors && series != null) {
      BookMetadataRow(
        label = stringResource(R.string.book_series),
        value = series,
        enabled = enabled,
        onClick = onSeriesClick.takeIf { inLibrary }
      )
    }
    language?.let { language ->
      BookMetadataRow(
        label = stringResource(R.string.language),
        value = language,
        enabled = false
      )
    }
    pageCount?.let { pageCount ->
      BookMetadataRow(
        label = stringResource(R.string.page_count),
        value = remember(pageCount) { pageCount.toLocaleString() }
      )
    }
    BookMetadataRow(
      label = stringResource(R.string.group),
      value = group,
      enabled = enabled,
      onClick = onGroupClick.takeIf { inLibrary }
    )
    BookMetadataRow(
      label = stringResource(R.string.dimensions),
      value = dimensions,
      enabled = false
    )
    BookMetadataRow(
      label = stringResource(R.string.label_price),
      value = labelPrice,
      enabled = false
    )
    BookMetadataRow(
      label = stringResource(R.string.paid_price),
      value = paidPrice,
      enabled = false
    )
    BookMetadataRow(
      label = stringResource(R.string.store),
      value = store,
      enabled = enabled,
      onClick = onStoreClick.takeIf { inLibrary }
    )
    if (boughtAt != null) {
      BookMetadataRow(
        label = stringResource(R.string.bought_at),
        value = boughtAt,
        onClick = onBoughtAtClick.takeIf { inLibrary }
      )
    }
    if (latestReading != null) {
      BookMetadataRow(
        label = stringResource(R.string.latest_reading),
        value = latestReading,
        onClick = onLatestReadingClick.takeIf { inLibrary }
      )
    }
  }
}

@Composable
fun BookMetadataRow(
  modifier: Modifier = Modifier,
  label: String,
  value: String,
  enabled: Boolean = true,
  onClick: (() -> Unit)? = null
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(
        enabled = enabled && onClick != null,
        onClick = onClick ?: {}
      )
      .padding(horizontal = 24.dp, vertical = 10.dp)
      .then(modifier)
  ) {
    Text(
      modifier = Modifier.placeholder(value.isEmpty()),
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
    Text(
      modifier = Modifier.placeholder(value.isEmpty()),
      text = value.ifEmpty { "Metadata value" },
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}