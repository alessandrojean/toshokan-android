package io.github.alessandrojean.toshokan.presentation.ui.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.ui.groups.GroupsScreen
import io.github.alessandrojean.toshokan.presentation.ui.people.PeopleScreen
import io.github.alessandrojean.toshokan.presentation.ui.publishers.PublishersScreen
import io.github.alessandrojean.toshokan.presentation.ui.settings.SettingsScreen
import io.github.alessandrojean.toshokan.presentation.ui.stores.StoresScreen
import io.github.alessandrojean.toshokan.presentation.ui.theme.DividerOpacity

class MoreScreen : AndroidScreen() {

  @Composable
  override fun Content() {
    val topAppBarScrollState = rememberTopAppBarScrollState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior(topAppBarScrollState) }
    val listState = rememberLazyListState()
    val navigator = LocalNavigator.currentOrThrow

    val topAppBarBackgroundColors = TopAppBarDefaults.smallTopAppBarColors()
    val topAppBarBackground = topAppBarBackgroundColors.containerColor(scrollBehavior.scrollFraction).value

    Scaffold(
      modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
      topBar = {
        Surface(color = topAppBarBackground) {
          SmallTopAppBar(
            modifier = Modifier.statusBarsPadding(),
            colors = TopAppBarDefaults.smallTopAppBarColors(
              containerColor = Color.Transparent,
              scrolledContainerColor = Color.Transparent
            ),
            scrollBehavior = scrollBehavior,
            title = { Text(stringResource(R.string.more)) }
          )
        }
      },
      content = { innerPadding ->
        LazyColumn(
          state = listState,
          contentPadding = innerPadding
        ) {
          item {
            NavigationItem(
              title = stringResource(R.string.publishers),
              icon = Icons.Outlined.Domain,
              onClick = { navigator.push(PublishersScreen()) }
            )
          }
          item {
            NavigationItem(
              title = stringResource(R.string.people),
              icon = Icons.Outlined.Group,
              onClick = { navigator.push(PeopleScreen()) }
            )
          }
          item {
            NavigationItem(
              title = stringResource(R.string.stores),
              icon = Icons.Outlined.LocalMall,
              onClick = { navigator.push(StoresScreen()) }
            )
          }
          item {
            NavigationItem(
              title = stringResource(R.string.groups),
              icon = Icons.Outlined.GroupWork,
              onClick = { navigator.push(GroupsScreen()) }
            )

            Divider(
              modifier = Modifier.fillMaxWidth(),
              color = LocalContentColor.current.copy(alpha = DividerOpacity)
            )
          }
          item {
            NavigationItem(
              title = stringResource(R.string.settings),
              icon = Icons.Outlined.Settings,
              onClick = { navigator.push(SettingsScreen()) }
            )
          }
          item {
            NavigationItem(
              title = stringResource(R.string.about),
              icon = Icons.Outlined.Info,
              onClick = {}
            )
          }
        }
      }
    )
  }

  @Composable
  fun NavigationItem(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
  ) {
    Row(
      modifier = modifier
        .fillMaxWidth()
        .clickable(
          onClick = onClick
        )
        .padding(16.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = MaterialTheme.colorScheme.surfaceTint
      )
      Text(
        text = title,
        modifier = Modifier.padding(start = 24.dp)
      )
    }
  }

}
