package io.github.alessandrojean.toshokan.util.extension

import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import androidx.paging.compose.LazyPagingItems

fun <T : Any> LazyGridScope.items(
  items: LazyPagingItems<T>,
  key: ((item: T) -> Any)? = null,
  itemContent: @Composable LazyGridItemScope.(value: T?) -> Unit
) {
  items(
    count = items.itemCount,
    key = if (key == null) null else { index ->
      val item = items.peek(index)
      if (item == null) {
        index
      } else {
        key(item)
      }
    }
  ) { index ->
    itemContent(items[index])
  }
}