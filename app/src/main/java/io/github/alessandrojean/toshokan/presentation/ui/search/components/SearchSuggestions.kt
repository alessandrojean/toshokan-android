package io.github.alessandrojean.toshokan.presentation.ui.search.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NorthWest
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.util.extension.removeAccents
import java.util.Locale

@Composable
fun SearchSuggestions(
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(),
  query: String,
  suggestions: List<String>,
  limit: Int = 30,
  onSuggestionClick: (String) -> Unit,
  onSuggestionSelectClick: (String) -> Unit
) {
  val locale = Locale.getDefault()
  val queryNormalized = remember(query) { query.removeAccents() }
  val filteredSuggestions by remember(query, suggestions) {
    derivedStateOf {
      if (query.isBlank()) {
        emptyList()
      } else {
        suggestions
          .map { it.lowercase(locale) }
          .filter { it.removeAccents().startsWith(queryNormalized) }
          .take(limit)
      }
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .then(modifier),
    contentPadding = contentPadding
  ) {
    items(filteredSuggestions) { suggestion ->
      SearchSuggestionItem(
        query = query,
        suggestion = suggestion,
        onClick = { onSuggestionClick(suggestion) },
        onSelectClick = { onSuggestionSelectClick(suggestion) }
      )
    }
  }
}

@Composable
fun SearchSuggestionItem(
  modifier: Modifier = Modifier,
  query: String,
  suggestion: String,
  onClick: () -> Unit,
  onSelectClick: () -> Unit
) {
  val context = LocalContext.current

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(start = 16.dp, top = 2.dp, bottom = 2.dp, end = 6.dp)
      .semantics {
        customActions = listOf(
          CustomAccessibilityAction(
            label = context.getString(R.string.action_select_suggestion),
            action = {
              onSelectClick()
              true
            }
          )
        )
      }
      .then(modifier),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      painter = rememberVectorPainter(Icons.Outlined.Search),
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = buildAnnotatedString {
        append(suggestion.substring(0, query.length))
        if (suggestion.length > query.length) {
          withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
            append(suggestion.substring(query.length))
          }
        }
      },
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .weight(1f),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
    IconButton(
      modifier = Modifier.clearAndSetSemantics {},
      onClick = onSelectClick
    ) {
      Icon(
        painter = rememberVectorPainter(Icons.Outlined.NorthWest),
        contentDescription = stringResource(R.string.action_select_suggestion)
      )
    }
  }
}