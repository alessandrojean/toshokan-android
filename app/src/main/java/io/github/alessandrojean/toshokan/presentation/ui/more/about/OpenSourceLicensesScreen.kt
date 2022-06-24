package io.github.alessandrojean.toshokan.presentation.ui.more.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.ui.compose.HtmlText
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.util.htmlReadyLicenseContent
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.ui.settings.components.SettingsScaffold
import io.github.alessandrojean.toshokan.util.extension.plus

class OpenSourceLicensesScreen : AndroidScreen() {

  override val key = "open_source_licenses_screen"

  @Composable
  override fun Content() {
    val navigator = LocalNavigator.currentOrThrow

    var library by remember { mutableStateOf<Library?>(null) }

    // Using a custom dialog because the AboutLibraries is still using Material2.
    // TODO: Remove it when AboutLibraries start using Material3.
    LicenseDialog(
      library = library,
      onDismiss = { library = null }
    )

    SettingsScaffold(
      title = stringResource(R.string.open_source_licenses),
      onNavigationClick = { navigator.pop() }
    ) { innerPadding ->
      LibrariesContainer(
        modifier = Modifier.fillMaxSize(),
        contentPadding = innerPadding + WindowInsets.navigationBars.asPaddingValues(),
        padding = LibraryDefaults.libraryPadding(
          namePadding = PaddingValues()
        ),
        colors = LibraryDefaults.libraryColors(
          backgroundColor = MaterialTheme.colorScheme.background,
          contentColor = MaterialTheme.colorScheme.onBackground,
          badgeBackgroundColor = MaterialTheme.colorScheme.primary,
          badgeContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        onLibraryClick = { library = it }
      )
    }
  }

  @Composable
  private fun LicenseDialog(
    library: Library?,
    onDismiss: () -> Unit
  ) {
    if (library != null) {
      val scrollState = rememberScrollState()

      AlertDialog(
        modifier = Modifier.padding(vertical = 16.dp),
        onDismissRequest = onDismiss,
        confirmButton = {
          TextButton(onClick = onDismiss) {
            Text(stringResource(R.string.aboutlibs_ok))
          }
        },
        title = {
          Text(library.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        text = {
          Column(modifier = Modifier.verticalScroll(scrollState)) {
            HtmlText(
              html = library.licenses.firstOrNull()?.htmlReadyLicenseContent.orEmpty(),
              color = LocalContentColor.current,
            )
          }
        }
      )
    }
  }

}