package io.github.alessandrojean.toshokan.presentation.ui.book.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.R

@Composable
fun BookNotes(
  modifier: Modifier = Modifier,
  header: String = stringResource(R.string.notes),
  notes: String?
) {
  if (notes.orEmpty().isNotBlank()) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .then(modifier)
    ) {
      Text(
        modifier = Modifier
          .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 12.dp)
          .semantics { heading() },
        text = header,
        style = MaterialTheme.typography.titleLarge
      )
      SelectionContainer(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp)
      ) {
        Text(
          text = notes.orEmpty(),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}