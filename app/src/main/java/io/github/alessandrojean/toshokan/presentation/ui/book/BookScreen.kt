package io.github.alessandrojean.toshokan.presentation.ui.book

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.alessandrojean.toshokan.R

@Composable
fun BookScreen(bookId: Long) {
  Column {
    SmallTopAppBar(
      title = { Text(stringResource(R.string.library)) }
    )
  }
}
