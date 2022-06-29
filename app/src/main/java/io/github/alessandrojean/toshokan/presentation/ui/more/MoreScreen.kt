package io.github.alessandrojean.toshokan.presentation.ui.more

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.ui.core.components.EnhancedSmallTopAppBar
import io.github.alessandrojean.toshokan.presentation.ui.groups.GroupsScreen
import io.github.alessandrojean.toshokan.presentation.ui.more.about.AboutScreen
import io.github.alessandrojean.toshokan.presentation.ui.people.PeopleScreen
import io.github.alessandrojean.toshokan.presentation.ui.publishers.PublishersScreen
import io.github.alessandrojean.toshokan.presentation.ui.settings.SettingsScreen
import io.github.alessandrojean.toshokan.presentation.ui.settings.components.SettingsCategory
import io.github.alessandrojean.toshokan.presentation.ui.stores.StoresScreen
import io.github.alessandrojean.toshokan.presentation.ui.tags.TagsScreen
import io.github.alessandrojean.toshokan.presentation.ui.theme.DividerOpacity

class MoreScreen : AndroidScreen() {

  @Composable
  override fun Content() {
    val topAppBarScrollState = rememberTopAppBarScrollState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior(topAppBarScrollState) }
    val listState = rememberLazyListState()
    val navigator = LocalNavigator.currentOrThrow

    Scaffold(
      modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
      topBar = {
        EnhancedSmallTopAppBar(
          contentPadding = WindowInsets.statusBars.asPaddingValues(),
          scrollBehavior = scrollBehavior,
          title = { Text(stringResource(R.string.more)) }
        )
      },
      content = { innerPadding ->
        LazyColumn(
          state = listState,
          contentPadding = innerPadding
        ) {
          item("publishers") {
            SettingsCategory(
              title = stringResource(R.string.publishers),
              icon = Icons.Outlined.Domain,
              onClick = { navigator.push(PublishersScreen()) }
            )
          }
          item("people") {
            SettingsCategory(
              title = stringResource(R.string.people),
              icon = Icons.Outlined.Group,
              onClick = { navigator.push(PeopleScreen()) }
            )
          }
          item("stores") {
            SettingsCategory(
              title = stringResource(R.string.stores),
              icon = Icons.Outlined.LocalMall,
              onClick = { navigator.push(StoresScreen()) }
            )
          }
          item("groups") {
            SettingsCategory(
              title = stringResource(R.string.groups),
              icon = Icons.Outlined.GroupWork,
              onClick = { navigator.push(GroupsScreen()) }
            )
          }
          item("tags") {
            SettingsCategory(
              title = stringResource(R.string.tags),
              icon = Icons.Outlined.Label,
              onClick = { navigator.push(TagsScreen()) }
            )

            Divider(
              modifier = Modifier.fillMaxWidth(),
              color = LocalContentColor.current.copy(alpha = DividerOpacity)
            )
          }
          item("settings") {
            SettingsCategory(
              title = stringResource(R.string.settings),
              icon = Icons.Outlined.Settings,
              onClick = { navigator.push(SettingsScreen()) }
            )
          }
          item("about") {
            SettingsCategory(
              title = stringResource(R.string.about),
              icon = Icons.Outlined.Info,
              onClick = { navigator.push(AboutScreen()) }
            )
          }
        }
      }
    )
  }

}
