import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.ui.main.Navigation
import io.github.alessandrojean.toshokan.presentation.ui.main.TopScreen

@Composable
fun MainNavHost (startScreen: TopScreen = TopScreen.Library) {
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentDestination = navBackStackEntry?.destination
  val currentRoute = currentDestination?.route

  val (requestedHideBottomNav, requestHideBottomNav) = remember { mutableStateOf(false) }

  DisposableEffect(navBackStackEntry) {
    onDispose {
      requestHideBottomNav(false)
    }
  }

  Scaffold(
    content = { paddingValues ->
      Box {
        Navigation(
          navController = navController,
          startScreen = startScreen,
          requestHideNavigator = requestHideBottomNav,
          modifier = Modifier.padding(paddingValues)
        )
      }
    },
    bottomBar = {
      val isVisible = TopLevelRoutes.isTopLevelRoute(currentRoute) && !requestedHideBottomNav

      AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
      ) {
        NavigationBar {
          TopLevelRoutes.values.forEach {
            val isSelected = currentRoute?.startsWith(it.screen.route) ?: false

            NavigationBarItem(
              icon = {
                Icon(
                  if (isSelected) it.selectedIcon else it.unselectedIcon,
                  contentDescription = null
                )
              },
              label = {
                Text(stringResource(it.text), maxLines = 1, overflow = TextOverflow.Ellipsis)
              },
              selected = isSelected,
              onClick = {
                navController.navigate(it.screen.route) {
                  // Avoid multiple copies of the same destination when
                  // reselecting the same item.
                  launchSingleTop = true

                  // Restore state when reselecting a previously selected item.
                  restoreState = true

                  // Pop up to the start destination of the graph to
                  // avoid building up a large stack of destinations
                  // on the back stack as users select items.
                  popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                  }
                }
              }
            )
          }
        }
      }
    }
  )
}

private enum class TopLevelRoutes(
  val screen: TopScreen,
  @StringRes val text: Int,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector = selectedIcon
) {
  Library(
    TopScreen.Library,
    R.string.library,
    Icons.Default.CollectionsBookmark,
    Icons.Outlined.CollectionsBookmark
  ),

  Statistics(
    TopScreen.Statistics,
    R.string.statistics,
    Icons.Default.InsertChart,
    Icons.Outlined.InsertChart
  ),

  More(
    TopScreen.More,
    R.string.more,
    Icons.Default.MoreHoriz,
    Icons.Outlined.MoreHoriz
  );

  companion object {
    val values = values().toList()

    fun isTopLevelRoute(route: String?): Boolean {
      val nestedRoute = route?.substringAfter("/")
      return nestedRoute != null && values.any { it.screen.route == nestedRoute }
    }
  }
}
