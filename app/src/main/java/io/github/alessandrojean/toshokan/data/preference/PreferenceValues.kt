package io.github.alessandrojean.toshokan.data.preference

import android.os.Build
import androidx.annotation.StringRes
import io.github.alessandrojean.toshokan.R

enum class Theme(@StringRes val title: Int) {
  FOLLOW_SYSTEM(R.string.pref_theme_follow_system),
  DARK(R.string.pref_theme_dark),
  LIGHT(R.string.pref_theme_light);

  companion object {
    val themes: List<Theme>
      get () = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        values().toList()
      } else {
        listOf(DARK, LIGHT)
      }
  }
}
