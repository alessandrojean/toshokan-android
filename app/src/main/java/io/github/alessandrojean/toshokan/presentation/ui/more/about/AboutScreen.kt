package io.github.alessandrojean.toshokan.presentation.ui.more.about

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alessandrojean.toshokan.BuildConfig
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.ui.settings.components.GenericPreference
import io.github.alessandrojean.toshokan.presentation.ui.settings.components.SettingsListScaffold
import io.github.alessandrojean.toshokan.util.extension.formatToLocaleDateTime
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class AboutScreen : AndroidScreen() {

  override val key = "about_screen"

  @Composable
  override fun Content() {
    val navigator = LocalNavigator.currentOrThrow
    val uriHandler = LocalUriHandler.current

    SettingsListScaffold(
      title = stringResource(R.string.about),
      onNavigationClick = { navigator.pop() }
    ) {
      item("version") {
        GenericPreference(
          title = stringResource(R.string.version),
          summary = when {
            BuildConfig.DEBUG -> {
              "Debug ${BuildConfig.COMMIT_SHA} (${getFormattedBuildTime()})"
            }
            BuildConfig.PREVIEW -> {
              "Preview r${BuildConfig.COMMIT_COUNT} (${getFormattedBuildTime()})"
            }
            else -> {
              "Stable ${BuildConfig.VERSION_NAME} (${getFormattedBuildTime()})"
            }
          }
        )
      }

      item("github") {
        GenericPreference(
          title = stringResource(R.string.github),
          summary = GITHUB_URL,
          onClick = { uriHandler.openUri(GITHUB_URL) }
        )
      }

      item("open_source_licenses") {
        GenericPreference(
          title = stringResource(R.string.open_source_licenses),
          onClick = { navigator.push(OpenSourceLicensesScreen()) }
        )
      }
    }
  }

  private fun getFormattedBuildTime(): String {
    val formatted = runCatching {
      val buildTime = BUILD_TIME_FORMATTER.parse(BuildConfig.BUILD_TIME)

      buildTime!!.time.formatToLocaleDateTime(dateStyle = DateFormat.SHORT)
    }

    return formatted.getOrNull() ?: BuildConfig.BUILD_TIME
  }

  companion object {
    const val GITHUB_URL = "https://github.com/alessandrojean/toshokan-android"
    private val BUILD_TIME_FORMATTER by lazy {
      SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
      }
    }
  }

}