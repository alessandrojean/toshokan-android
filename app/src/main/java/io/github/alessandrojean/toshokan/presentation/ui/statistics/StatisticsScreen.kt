package io.github.alessandrojean.toshokan.presentation.ui.statistics

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.alessandrojean.toshokan.R

@Composable
fun StatisticsScreen() {
  Scaffold(
    topBar = {
      SmallTopAppBar(
        title = { Text(stringResource(R.string.statistics)) }
      )
    },
    content = { innerPadding ->
      Text(stringResource(R.string.statistics), modifier = Modifier.padding(innerPadding))
    }
  )
}
