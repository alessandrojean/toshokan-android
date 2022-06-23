package io.github.alessandrojean.toshokan.presentation.ui.main

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.InsertChart
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.FadeTransition
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.extensions.surfaceColorAtNavigationBarElevation
import io.github.alessandrojean.toshokan.presentation.ui.core.provider.LocalNavigationBarControl
import io.github.alessandrojean.toshokan.presentation.ui.core.provider.NavigationBarControl
import io.github.alessandrojean.toshokan.presentation.ui.library.LibraryScreen
import io.github.alessandrojean.toshokan.presentation.ui.more.MoreScreen
import io.github.alessandrojean.toshokan.presentation.ui.statistics.StatisticsScreen

@Composable
fun MainNavHost() {
  Navigator(remember { TopLevelRoutes.Library.screen }) { navigator ->
    var hideBottomNavigation by remember { mutableStateOf(false) }
    val isBottomBarVisible = TopLevelRoutes.isTopLevelRoute(navigator) && !hideBottomNavigation

    val systemUiController = rememberSystemUiController()
    val navigationBarColor = if (isBottomBarVisible || navigator.isEmpty) {
      MaterialTheme.colorScheme.surfaceColorAtNavigationBarElevation()
    } else {
      MaterialTheme.colorScheme.surfaceColorAtNavigationBarElevation().copy(alpha = 0.7f)
    }

    DisposableEffect(navigator.lastItem) {
      onDispose {
        hideBottomNavigation = false
      }
    }

    SideEffect {
      systemUiController.setNavigationBarColor(
        color = navigationBarColor
      )
    }

    val navigationBarControl = NavigationBarControl(
      show = { hideBottomNavigation = false },
      hide = { hideBottomNavigation = true }
    )

    Scaffold(
      modifier = Modifier.windowInsetsPadding(
        WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
      ),
      content = { innerPadding ->
        CompositionLocalProvider(LocalNavigationBarControl provides navigationBarControl) {
          FadeTransition(navigator) { screen ->
            Box(modifier = Modifier.padding(innerPadding)) {
              screen.Content()
            }
          }
        }
      },
      bottomBar = {
        AnimatedVisibility(
          visible = isBottomBarVisible,
          enter = fadeIn() + slideInVertically { it } +
            expandVertically(expandFrom = Alignment.Top, clip = false),
          exit = fadeOut() + slideOutVertically { it } +
            shrinkVertically(clip = false)
        ) {
          Box(
            modifier = Modifier
              .navigationBarsPadding()
              .background(MaterialTheme.colorScheme.surfaceColorAtNavigationBarElevation())
          ) {
            NavigationBar {
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
                      if (navigator.lastItem is LibraryScreen) {
                        navigator.push(it.screen)
                      } else if (it.screen !is LibraryScreen) {
                        navigator.replace(it.screen)
                      } else {
                        navigator.replaceAll(it.screen)
                      }
                    }
                  }
                )
              }
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
