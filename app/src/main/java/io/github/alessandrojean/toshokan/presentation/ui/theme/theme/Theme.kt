package io.github.alessandrojean.toshokan.presentation.ui.theme.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColorScheme(
  primary = Indigo500,
//  primaryVariant = Indigo700,
  secondary = Emerald500,
  onSecondary = Color.White
)

private val LightColorPalette = lightColorScheme(
  primary = Indigo600,
//  primaryVariant = Indigo700,
  secondary = Emerald500,
  onSecondary = Color.White

  /* Other default colors to override
  background = Color.White,
  surface = Color.White,
  onPrimary = Color.White,
  onSecondary = Color.Black,
  onBackground = Color.Black,
  onSurface = Color.Black,
  */
)

@Composable
fun ToshokanTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
  val colorScheme = if (darkTheme) {
    DarkColorPalette
  } else {
    LightColorPalette
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = Shapes,
    content = content
  )
}
