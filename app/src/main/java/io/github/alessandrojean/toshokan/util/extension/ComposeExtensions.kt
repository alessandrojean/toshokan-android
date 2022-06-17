package io.github.alessandrojean.toshokan.util.extension

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp

@Composable
fun PaddingValues.copy(
  top: Dp = this.calculateTopPadding(),
  bottom: Dp = this.calculateBottomPadding(),
  start: Dp = this.calculateStartPadding(LocalLayoutDirection.current),
  end: Dp = this.calculateEndPadding(LocalLayoutDirection.current)
): PaddingValues {
  return PaddingValues(
    top = top,
    bottom = bottom,
    start = start,
    end = end
  )
}

val PaddingValues.top: Dp
  @Composable get() = calculateTopPadding()

val PaddingValues.bottom: Dp
  @Composable get() = calculateBottomPadding()

val PaddingValues.start: Dp
  @Composable get() = calculateStartPadding(LocalLayoutDirection.current)

val PaddingValues.end: Dp
  @Composable get() = calculateEndPadding(LocalLayoutDirection.current)

val WindowInsets.Companion.navigationBarsWithIme: WindowInsets
  @Composable get() = navigationBars.union(WindowInsets.ime)

val WindowInsets.bottomPadding
  @Composable get() = asPaddingValues().bottom

fun Modifier.navigationBarsWithImePadding() =
  composed { windowInsetsPadding(WindowInsets.navigationBarsWithIme) }