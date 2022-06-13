package io.github.alessandrojean.toshokan.presentation.ui.settings.library

import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.data.preference.PreferenceKeys
import io.github.alessandrojean.toshokan.presentation.ui.settings.components.SwitchPreference
import io.github.alessandrojean.toshokan.util.extension.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

class LibrarySettingsScreen : AndroidScreen() {

  @Composable
  override fun Content() {
    val viewModel = getViewModel<LibrarySettingsViewModel>()
    val navigator = LocalNavigator.currentOrThrow
    val topAppBarScrollState = rememberTopAppBarScrollState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior(topAppBarScrollState) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val showBookNavigation by viewModel.showBookNavigation.asFlow()
      .collectAsStateWithLifecycle(initialValue = true)

    Scaffold(
      modifier = Modifier
        .nestedScroll(scrollBehavior.nestedScrollConnection)
        .systemBarsPadding(),
      topBar = {
        SmallTopAppBar(
          scrollBehavior = scrollBehavior,
          navigationIcon = {
            IconButton(onClick = { navigator.pop() }) {
              Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.action_back)
              )
            }
          },
          title = {
            Text(
              text = stringResource(R.string.settings_library),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        )
      },
      content = { innerPadding ->
        LazyColumn(
          state = listState,
          contentPadding = innerPadding
        ) {
          item(PreferenceKeys.showBookNavigation) {
            SwitchPreference(
              title = stringResource(R.string.pref_show_book_navigation),
              checked = showBookNavigation,
              onCheckedChange = {
                scope.launch { viewModel.showBookNavigation.setAndCommit(it) }
              }
            )
          }
        }
      }
    )
  }

}