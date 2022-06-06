package io.github.alessandrojean.toshokan.presentation.extensions

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ln

/**
 * https://github.com/androidx/androidx/blob/androidx-main/compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/ColorScheme.kt#L476-L482
 */
@Composable
fun ColorScheme.surfaceColorAtNavigationBarElevation(): Color {
  val elevation = LocalAbsoluteTonalElevation.current + 3.dp
  return surfaceWithTonalElevation(elevation)
}

@Composable
fun ColorScheme.surfaceWithTonalElevation(tonalElevation: Dp): Color {
  return surface.withTonalElevation(tonalElevation)
}


@Composable
fun Color.withTonalElevation(tonalElevation: Dp): Color {
  if (tonalElevation == 0.dp) {
    return this
  }

  val alpha = ((4.5f * ln(tonalElevation.value + 1)) + 2f) / 100f
  return MaterialTheme.colorScheme.primary.copy(alpha = alpha).compositeOver(this)
}