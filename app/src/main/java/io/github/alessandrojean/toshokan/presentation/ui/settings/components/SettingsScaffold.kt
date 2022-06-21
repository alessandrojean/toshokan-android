package io.github.alessandrojean.toshokan.presentation.ui.settings.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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

@Composable
fun SettingsScaffold(
  modifier: Modifier = Modifier,
  title: String,
  onNavigationClick: () -> Unit,
  content: LazyListScope.() -> Unit
) {
  val topAppBarScrollState = rememberTopAppBarScrollState()
  val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior(topAppBarScrollState) }
  val listState = rememberLazyListState()

  Scaffold(
    modifier = Modifier
      .nestedScroll(scrollBehavior.nestedScrollConnection)
      .navigationBarsPadding()
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
    content = { innerPadding ->
      LazyColumn(
        state = listState,
        contentPadding = innerPadding,
        content = content
      )
    }
  )
}