package io.github.alessandrojean.toshokan.presentation.ui.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.ui.groups.GroupsScreen
import io.github.alessandrojean.toshokan.presentation.ui.people.PeopleScreen
import io.github.alessandrojean.toshokan.presentation.ui.publishers.PublishersScreen
import io.github.alessandrojean.toshokan.presentation.ui.stores.StoresScreen
import io.github.alessandrojean.toshokan.presentation.ui.theme.DividerOpacity

class MoreScreen : AndroidScreen() {

  private data class Destination(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val topDivider: Boolean = false
  )

  @Composable
  override fun Content() {
    val topAppBarScrollState = rememberTopAppBarScrollState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior(topAppBarScrollState) }
    val listState = rememberLazyListState()
    val navigator = LocalNavigator.currentOrThrow

    val destinations = listOf(
      Destination(
        title = stringResource(R.string.publishers),
        icon = Icons.Outlined.Domain,
        onClick = { navigator.push(PublishersScreen()) }
      ),
      Destination(
        title = stringResource(R.string.people),
        icon = Icons.Outlined.Group,
        onClick = { navigator.push(PeopleScreen()) }
      ),
      Destination(
        title = stringResource(R.string.stores),
        icon = Icons.Outlined.LocalMall,
        onClick = { navigator.push(StoresScreen()) }
      ),
      Destination(
        title = stringResource(R.string.groups),
        icon = Icons.Outlined.GroupWork,
        onClick = { navigator.push(GroupsScreen()) }
      ),
      Destination(
        title = stringResource(R.string.settings),
        icon = Icons.Outlined.Settings,
        onClick = {},
        topDivider = true
      ),
      Destination(
        title = stringResource(R.string.about),
        icon = Icons.Outlined.Info,
        onClick = {}
      )
    )

    val systemUiController = rememberSystemUiController()
    val statusBarColor = when {
      scrollBehavior.scrollFraction > 0 -> TopAppBarDefaults
        .smallTopAppBarColors()
        .containerColor(scrollBehavior.scrollFraction)
        .value
      else -> MaterialTheme.colorScheme.surface
    }

    SideEffect {
      systemUiController.setStatusBarColor(
        color = statusBarColor
      )
    }

    Scaffold(
      modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
      topBar = {
        SmallTopAppBar(
          modifier = Modifier.statusBarsPadding(),
          title = { Text(stringResource(R.string.more)) },
          scrollBehavior = scrollBehavior
        )
      },
      content = { innerPadding ->
        LazyColumn(
          state = listState,
          contentPadding = innerPadding
        ) {
          items(destinations) { destination ->
            if (destination.topDivider) {
              Divider(
                modifier = Modifier.fillMaxWidth(),
                color = LocalContentColor.current.copy(alpha = DividerOpacity)
              )
            }

            NavigationItem(
              title = destination.title,
              icon = destination.icon,
              onClick = destination.onClick
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
