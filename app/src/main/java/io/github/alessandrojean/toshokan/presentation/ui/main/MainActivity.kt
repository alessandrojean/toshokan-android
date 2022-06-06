package io.github.alessandrojean.toshokan.presentation.ui.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Turn off the decor fitting system windows.
    // Insets should be manually handled.
    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent {
      MainApp()
    }
  }
}



@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  MainApp()
}
