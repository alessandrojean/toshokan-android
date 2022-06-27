package io.github.alessandrojean.toshokan.presentation.ui.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import io.github.alessandrojean.toshokan.R

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

  /**
   * To be checked by the splash screen.
   * If true then the splash screen will be removed.
   */
  var ready = false

  override fun onCreate(savedInstanceState: Bundle?) {
    val splashScreen = if (savedInstanceState == null) {
      installSplashScreen()
    } else {
      setTheme(R.style.Theme_Toshokan)
      null
    }

    super.onCreate(savedInstanceState)

    // Turn off the decor fitting system windows.
    // Insets should be manually handled.
    WindowCompat.setDecorFitsSystemWindows(window, false)

    val startTime = System.currentTimeMillis()
    splashScreen?.setKeepOnScreenCondition {
      val elapsed = System.currentTimeMillis() - startTime
      elapsed <= SPLASH_MIN_DURATION || (!ready && elapsed <= SPLASH_MAX_DURATION)
    }

    setContent {
      MainApp()
    }

    ready = true
  }

  companion object {
    // Splash screen
    private const val SPLASH_MIN_DURATION = 500 // ms
    private const val SPLASH_MAX_DURATION = 5000 // ms
  }
}



@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  MainApp()
}
