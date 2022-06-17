package io.github.alessandrojean.toshokan.presentation.ui.library.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.database.data.Book
import io.github.alessandrojean.toshokan.presentation.ui.core.components.BookCard

@Composable
fun LibraryGrid(
  modifier: Modifier = Modifier,
  books: List<Book>,
  contentPadding: PaddingValues = PaddingValues(4.dp),
  columns: GridCells = GridCells.Adaptive(minSize = 96.dp),
  onBookClick: (Book) -> Unit,
) {
  LazyVerticalGrid(
    modifier = modifier,
    columns = columns,
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(4.dp),
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    items(books, key = { it.id }) { book ->
      BookCard(
        modifier = Modifier.fillMaxWidth(),
        book = book,
        onClick = { onBookClick.invoke(book) }
      )
    }
  }
}