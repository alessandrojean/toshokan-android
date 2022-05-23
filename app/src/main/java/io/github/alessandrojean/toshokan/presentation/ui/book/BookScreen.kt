package io.github.alessandrojean.toshokan.presentation.ui.book

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.alessandrojean.toshokan.R

@Composable
fun BookScreen(bookId: Long) {
  Column {
    SmallTopAppBar(
      modifier = Modifier.statusBarsPadding(),
      title = { Text(stringResource(R.string.library)) }
    )
  }
}
