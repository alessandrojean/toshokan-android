package io.github.alessandrojean.toshokan.presentation.ui.library.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import io.github.alessandrojean.toshokan.database.data.Book
import io.github.alessandrojean.toshokan.presentation.ui.core.components.BookCard

@Composable
fun LibraryGrid(
  modifier: Modifier = Modifier,
  books: LazyPagingItems<Book>,
  selection: SnapshotStateList<Long> = mutableStateListOf(),
  state: LazyGridState = rememberLazyGridState(),
  contentPadding: PaddingValues = PaddingValues(4.dp),
  columns: GridCells = GridCells.Adaptive(minSize = 96.dp),
  onBookClick: (Book) -> Unit,
  onBookLongClick: (Book) -> Unit
) {
  if (books.itemCount > 0) {
    LazyVerticalGrid(
      modifier = modifier,
      state = state,
      columns = columns,
      contentPadding = contentPadding,
      verticalArrangement = Arrangement.spacedBy(4.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      items(books.itemCount) { index ->
        val book = books[index]

        BookCard(
          modifier = Modifier.fillMaxWidth(),
          book = book,
          selected = book?.id in selection,
          onClick = {
            book?.let { onBookClick.invoke(it) }
          },
          onLongClick = {
            book?.let { onBookLongClick.invoke(it) }
          }
        )
      }
    }
  }
}