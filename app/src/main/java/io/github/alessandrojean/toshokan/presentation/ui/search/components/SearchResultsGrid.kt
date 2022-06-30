package io.github.alessandrojean.toshokan.presentation.ui.search.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.database.data.Book
import io.github.alessandrojean.toshokan.presentation.ui.core.components.BookCard

@Composable
fun SearchResultsGrid(
  modifier: Modifier,
  results: List<Book>,
  contentPadding: PaddingValues = PaddingValues(4.dp),
  columns: GridCells = GridCells.Adaptive(minSize = 96.dp),
  state: LazyGridState = rememberLazyGridState(),
  onResultClick: (Book) -> Unit
) {
  LazyVerticalGrid(
    modifier = modifier,
    columns = columns,
    state = state,
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    items(results, key = { it.id }) { book ->
      BookCard(
        modifier = Modifier
          .fillMaxWidth()
          .animateItemPlacement(),
        book = book,
        onClick = { onResultClick.invoke(book) }
      )
    }
  }
}
