package io.github.alessandrojean.toshokan.presentation.ui.book

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.core.screen.Screen
import io.github.alessandrojean.toshokan.R

data class BookScreen(val bookId: Long) : AndroidScreen() {

  @Composable
  override fun Content() {
    Scaffold(
      topBar = {
        SmallTopAppBar(
          modifier = Modifier.statusBarsPadding(),
          title = { Text(stringResource(R.string.library)) }
        )
      },
      content = { innerPaddings ->
        Text(bookId.toString(), modifier = Modifier.padding(innerPaddings))
      }
    )
  }

}
