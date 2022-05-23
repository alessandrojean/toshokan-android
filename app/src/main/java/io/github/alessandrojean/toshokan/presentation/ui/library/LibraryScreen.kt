package io.github.alessandrojean.toshokan.presentation.ui.library

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.alessandrojean.toshokan.R

@Composable
fun LibraryScreen(
  requestHideNavigator: (Boolean) -> Unit,
  openBook: (Long) -> Unit,
  createNewBook: () -> Unit
) {
  Scaffold(
    topBar = {
      SmallTopAppBar(
        modifier = Modifier.windowInsetsPadding(
          WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
        ),
        title = { Text(stringResource(R.string.library)) },
        actions = {
          IconButton(onClick = { /*TODO*/ }) {
            Icon(
              Icons.Default.Search,
              contentDescription = stringResource(R.string.action_search)
            )
          }
        }
      )
    },
    content = { innerPadding ->
      Text(stringResource(R.string.library), modifier = Modifier.padding(innerPadding))
    },
    floatingActionButtonPosition = FabPosition.End,
    floatingActionButton = {
      FloatingActionButton(onClick = createNewBook) {
        Icon(
          Icons.Default.Add,
          contentDescription = stringResource(R.string.action_new_book)
        )
      }
    }
  )
}
