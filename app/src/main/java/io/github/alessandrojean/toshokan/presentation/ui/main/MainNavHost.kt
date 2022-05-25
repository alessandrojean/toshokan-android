package io.github.alessandrojean.toshokan.presentation.ui.main

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.FadeTransition
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.extensions.surfaceColorAtNavigationBarElevation
import io.github.alessandrojean.toshokan.presentation.ui.library.LibraryScreen
import io.github.alessandrojean.toshokan.presentation.ui.more.MoreScreen
import io.github.alessandrojean.toshokan.presentation.ui.statistics.StatisticsScreen

@Composable
fun MainNavHost() {
  val navigationBarHeight = WindowInsets.navigationBars
    .asPaddingValues()
    .calculateBottomPadding()
    .value.toInt()

  Navigator(TopLevelRoutes.Library.screen) { navigator ->
    val (requestedHideBottomNav, requestHideBottomNav) = remember { mutableStateOf(false) }

    val isBottomBarVisible by remember {
      derivedStateOf {
        TopLevelRoutes.isTopLevelRoute(navigator) && !requestedHideBottomNav
      }
    }

    val systemUiController = rememberSystemUiController()
    val navigationBarColor = if (isBottomBarVisible || navigator.isEmpty) {
      MaterialTheme.colorScheme.surfaceColorAtNavigationBarElevation()
    } else {
      Color.Transparent
    }

    DisposableEffect(navigator.lastItem) {
      onDispose {
        requestHideBottomNav(false)
      }
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
        FadeTransition(navigator) { screen ->
          Box(modifier = Modifier.padding(innerPadding)) {
            screen.Content()
          }
        }
      },
      bottomBar = {
        AnimatedVisibility(
          visible = isBottomBarVisible,
          enter = fadeIn() + slideInVertically(initialOffsetY = { it + navigationBarHeight }) +
            expandVertically(expandFrom = Alignment.Top, initialHeight = { 0 }, clip = false),
          exit = fadeOut() + slideOutVertically(targetOffsetY = { it + navigationBarHeight }) +
            shrinkVertically(targetHeight = { 0 }, clip = false)
        ) {
          NavigationBar(
            modifier = Modifier.navigationBarsPadding()
          ) {
            TopLevelRoutes.values.forEach {
              val isSelected = navigator.lastItem.key == it.screen.key

              NavigationBarItem(
                icon = {
                  Icon(
                    if (isSelected) it.selectedIcon else it.unselectedIcon,
                    contentDescription = null
                  )
                },
                label = {
                  Text(
                    text = stringResource(it.text),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                },
                selected = isSelected,
                onClick = {
                  if (!isSelected) {
                    navigator.replace(it.screen)
                  }
                }
              )
            }
          }
        }
      }
    )
  }
}

private enum class TopLevelRoutes(
  val screen: Screen,
  @StringRes val text: Int,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector = selectedIcon
) {
  Library(
    LibraryScreen(),
    R.string.library,
    Icons.Default.CollectionsBookmark,
    Icons.Outlined.CollectionsBookmark
  ),

  Statistics(
    StatisticsScreen(),
    R.string.statistics,
    Icons.Default.InsertChart,
    Icons.Outlined.InsertChart
  ),

  More(
    MoreScreen(),
    R.string.more,
    Icons.Default.MoreHoriz,
    Icons.Outlined.MoreHoriz
  );

  companion object {
    val values = values().toList()

    fun isTopLevelRoute(navigator: Navigator): Boolean {
      return values.any { it.screen.key == navigator.lastItemOrNull?.key }
    }
  }
}
