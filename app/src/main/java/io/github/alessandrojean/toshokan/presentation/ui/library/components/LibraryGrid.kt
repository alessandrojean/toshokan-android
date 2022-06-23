package io.github.alessandrojean.toshokan.presentation.ui.library.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.alessandrojean.toshokan.database.data.Book
import io.github.alessandrojean.toshokan.presentation.ui.core.components.BookCard
import io.github.alessandrojean.toshokan.util.extension.items
import kotlinx.coroutines.flow.Flow

@Composable
fun LibraryGrid(
  modifier: Modifier = Modifier,
  books: LazyPagingItems<Book>,
  state: LazyGridState = rememberLazyGridState(),
  contentPadding: PaddingValues = PaddingValues(4.dp),
  columns: GridCells = GridCells.Adaptive(minSize = 96.dp),
  onBookClick: (Book) -> Unit,
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
          onClick = {
            book?.let { onBookClick.invoke(it) }
          }
        )
      }
    }
  }

//    items(books, key = { it.id }) { book ->
//      BookCard(
//        modifier = Modifier.fillMaxWidth(),
//        book = book,
//        onClick = { onBookClick.invoke(book) }
//      )
//    }
}