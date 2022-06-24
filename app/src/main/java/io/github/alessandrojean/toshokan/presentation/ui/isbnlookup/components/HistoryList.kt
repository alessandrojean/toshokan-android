package io.github.alessandrojean.toshokan.presentation.ui.isbnlookup.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.R

@Composable
fun HistoryList(
  modifier: Modifier = Modifier,
  history: List<String>,
  contentPadding: PaddingValues,
  onClick: (String) -> Unit,
  onRemoveClick: (String) -> Unit
) {
  LazyColumn(
    state = rememberLazyListState(),
    contentPadding = contentPadding,
    modifier = Modifier
      .fillMaxSize()
      .then(modifier)
  ) {
    items(history, key = { it }) { isbn ->
      HistoryItem(
        modifier = Modifier
          .fillMaxWidth()
          .animateItemPlacement(),
        text = isbn,
        onClick = { onClick.invoke(isbn) },
        onRemoveClick = { onRemoveClick.invoke(isbn) }
      )
    }
  }
}

@Composable
fun HistoryItem(
  modifier: Modifier = Modifier,
  text: String,
  onClick: () -> Unit,
  onRemoveClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .clickable(onClick = onClick)
      .padding(start = 16.dp, top = 4.dp, bottom = 4.dp, end = 4.dp)
      .then(modifier),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = Icons.Outlined.History,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      modifier = Modifier
        .padding(start = 24.dp)
        .weight(1f),
      text = text,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
    IconButton(onClick = onRemoveClick) {
      Icon(
        imageVector = Icons.Outlined.Delete,
        contentDescription = stringResource(R.string.action_clear)
      )
    }
  }
}