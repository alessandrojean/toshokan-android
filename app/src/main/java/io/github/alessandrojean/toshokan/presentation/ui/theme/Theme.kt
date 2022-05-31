package io.github.alessandrojean.toshokan.presentation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

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

const val DividerOpacity = 0.12f

val DialogTitlePadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp)
val DialogButtonBoxPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 18.dp)

@Composable
fun ToshokanTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
  val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

  val colorScheme = when {
    dynamicColor && darkTheme -> dynamicDarkColorScheme(LocalContext.current)
    dynamicColor && !darkTheme -> dynamicLightColorScheme(LocalContext.current)
    darkTheme -> DarkColorPalette
    else -> LightColorPalette
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = Shapes,
    content = content
  )
}
