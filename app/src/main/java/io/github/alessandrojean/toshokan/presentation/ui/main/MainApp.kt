package io.github.alessandrojean.toshokan.presentation.ui.main

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.github.alessandrojean.toshokan.presentation.ui.theme.ToshokanTheme

@Composable
fun MainApp() {
  val systemUiController = rememberSystemUiController()
  val useDarkIcons = !isSystemInDarkTheme()

  SideEffect {
    // The navigation bar color is handled by the navigation host.
    systemUiController.setStatusBarColor(
      color = Color.Transparent,
      darkIcons = useDarkIcons
    )
  }

  ToshokanTheme {
    MainNavHost()
  }
}
