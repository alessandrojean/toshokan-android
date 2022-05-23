package io.github.alessandrojean.toshokan.presentation.ui.main

import MainNavHost
import androidx.compose.runtime.Composable
import io.github.alessandrojean.toshokan.presentation.ui.theme.ToshokanTheme

@Composable
fun MainApp() {
  ToshokanTheme {
    MainNavHost()
  }
}
