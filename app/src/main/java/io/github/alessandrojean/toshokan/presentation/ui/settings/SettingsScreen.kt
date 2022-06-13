package io.github.alessandrojean.toshokan.presentation.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.ManageSearch
import androidx.compose.material.icons.outlined.SettingsBackupRestore
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.ui.settings.components.SettingsCategory
import io.github.alessandrojean.toshokan.presentation.ui.settings.library.LibrarySettingsScreen

class SettingsScreen : AndroidScreen() {

  private data class Category(
    @StringRes val title: Int,
    val icon: ImageVector,
    val navigate: (Navigator) -> Unit
  )

  private val categories = arrayOf(
    Category(
      title = R.string.settings_general,
      icon = Icons.Outlined.Tune,
      navigate = {}
    ),
    Category(
      title = R.string.settings_library,
      icon = Icons.Outlined.CollectionsBookmark,
      navigate = { it.push(LibrarySettingsScreen()) }
    ),
    Category(
      title = R.string.settings_search,
      icon = Icons.Outlined.ManageSearch,
      navigate = {}
    ),
    Category(
      title = R.string.settings_backup,
      icon = Icons.Outlined.SettingsBackupRestore,
      navigate = {}
    ),
    Category(
      title = R.string.settings_advanced,
      icon = Icons.Outlined.Code,
      navigate = {}
    )
  )

  @Composable
  override fun Content() {
    val navigator = LocalNavigator.currentOrThrow
    val topAppBarScrollState = rememberTopAppBarScrollState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior(topAppBarScrollState) }
    val listState = rememberLazyListState()

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
              text = stringResource(R.string.settings),
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
          items(categories) { category ->
            SettingsCategory(
              title = stringResource(category.title),
              icon = category.icon,
              onClick = { category.navigate.invoke(navigator) }
            )
          }
        }
      }
    )
  }

}