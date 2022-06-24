package io.github.alessandrojean.toshokan.presentation.ui.settings.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.ui.core.components.EnhancedSmallTopAppBar
import io.github.alessandrojean.toshokan.util.extension.plus

@Composable
fun SettingsListScaffold(
  modifier: Modifier = Modifier,
  title: String,
  onNavigationClick: () -> Unit,
  listState: LazyListState = rememberLazyListState(),
  content: LazyListScope.() -> Unit
) {
  SettingsScaffold(
    modifier = modifier,
    title = title,
    onNavigationClick = onNavigationClick,
    content = { innerPadding ->
      LazyColumn(
        state = listState,
        contentPadding = innerPadding + WindowInsets.navigationBars.asPaddingValues(),
        content = content
      )
    }
  )
}

@Composable
fun SettingsScaffold(
  modifier: Modifier = Modifier,
  title: String,
  onNavigationClick: () -> Unit,
  content: @Composable (PaddingValues) -> Unit
) {
  val topAppBarScrollState = rememberTopAppBarScrollState()
  val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior(topAppBarScrollState) }

  Scaffold(
    modifier = Modifier
      .nestedScroll(scrollBehavior.nestedScrollConnection)
      .then(modifier),
    topBar = {
      EnhancedSmallTopAppBar(
        contentPadding = WindowInsets.statusBars.asPaddingValues(),
        scrollBehavior = scrollBehavior,
        navigationIcon = {
          IconButton(onClick = onNavigationClick) {
            Icon(
              imageVector = Icons.Outlined.ArrowBack,
              contentDescription = stringResource(R.string.action_back)
            )
          }
        },
        title = {
          Text(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      )
    },
    content = content
  )
}