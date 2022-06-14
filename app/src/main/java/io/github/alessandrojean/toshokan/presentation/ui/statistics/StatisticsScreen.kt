package io.github.alessandrojean.toshokan.presentation.ui.statistics

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.androidx.AndroidScreen
import io.github.alessandrojean.toshokan.R

class StatisticsScreen : AndroidScreen() {

  @Composable
  override fun Content() {
    Scaffold(
      topBar = {
        SmallTopAppBar(
          modifier = Modifier.windowInsetsPadding(
            WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
          ),
          title = { Text(stringResource(R.string.statistics)) }
        )
      },
      content = { innerPadding ->
        Text(stringResource(R.string.statistics), modifier = Modifier.padding(innerPadding))
      }
    )
  }

}
