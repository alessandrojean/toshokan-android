package io.github.alessandrojean.toshokan.presentation.ui.core.provider

import androidx.compose.runtime.compositionLocalOf

data class NavigationBarControl(
  val show: () -> Unit = {},
  val hide: () -> Unit = {}
)

val LocalNavigationBarControl = compositionLocalOf { NavigationBarControl() }
