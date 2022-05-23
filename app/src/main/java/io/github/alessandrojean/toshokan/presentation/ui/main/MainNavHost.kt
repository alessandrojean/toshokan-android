package io.github.alessandrojean.toshokan.presentation.ui.main

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.extensions.surfaceColorAtNavigationBarElevation

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

  val isBottomBarVisible = TopLevelRoutes.isTopLevelRoute(currentRoute) && !requestedHideBottomNav
  val bottomBarOffset by animateFloatAsState(if (isBottomBarVisible) 0f else 80f)

  val systemUiController = rememberSystemUiController()
  val navigationBarColor = if (isBottomBarVisible) {
    MaterialTheme.colorScheme.surfaceColorAtNavigationBarElevation()
  } else {
    Color.Transparent
  }

  SideEffect {
    systemUiController.setNavigationBarColor(
      color = navigationBarColor
    )
  }

  Scaffold(
    modifier = Modifier.windowInsetsPadding(
      WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
    ),
    content = { innerPadding ->
      Box(modifier = Modifier.padding(innerPadding)) {
        Navigation(
          navController = navController,
          startScreen = startScreen,
          requestHideNavigator = requestHideBottomNav
        )
      }
    },
    bottomBar = {
      AnimatedVisibility(
        visible = isBottomBarVisible,
        enter = expandVertically(expandFrom = Alignment.Bottom, initialHeight = { 0 }) +
          slideInVertically(initialOffsetY = { it }) +
          fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) +
          shrinkVertically(targetHeight = { 0 }) +
          fadeOut()
      ) {
        NavigationBar(
          modifier = Modifier.navigationBarsPadding()
        ) {
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
