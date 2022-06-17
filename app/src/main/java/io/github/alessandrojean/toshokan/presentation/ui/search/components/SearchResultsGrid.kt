package io.github.alessandrojean.toshokan.presentation.ui.search.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.database.data.Book
import io.github.alessandrojean.toshokan.presentation.ui.core.components.BookCard

@Composable
fun SearchResultsGrid(
  modifier: Modifier,
  results: SnapshotStateList<Book>,
  contentPadding: PaddingValues = PaddingValues(4.dp),
  columns: GridCells = GridCells.Adaptive(minSize = 96.dp),
  onResultClick: (Book) -> Unit
) {
  val gridState = rememberLazyGridState()

  LazyVerticalGrid(
    modifier = modifier,
    columns = columns,
    state = gridState,
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    items(results, key = { it.id }) { book ->
      BookCard(
        modifier = Modifier
          .fillMaxWidth()
          .animateItemPlacement(),
        title = book.title,
        coverUrl = book.cover_url,
        isFuture = book.is_future,
        onClick = { onResultClick.invoke(book) }
      )
    }
  }
}
