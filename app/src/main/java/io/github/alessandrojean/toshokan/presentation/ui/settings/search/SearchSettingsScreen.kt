package io.github.alessandrojean.toshokan.presentation.ui.settings.search

import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.core.net.toUri
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.ui.settings.components.SettingsHeader
import io.github.alessandrojean.toshokan.presentation.ui.settings.components.SettingsScaffold
import io.github.alessandrojean.toshokan.presentation.ui.settings.components.SwitchPreference
import io.github.alessandrojean.toshokan.service.lookup.Provider
import io.github.alessandrojean.toshokan.util.extension.collectAsStateWithLifecycle
import java.text.Collator
import java.util.Locale

class SearchSettingsScreen : AndroidScreen() {

  @Composable
  override fun Content() {
    val viewModel = getViewModel<SearchSettingsViewModel>()
    val navigator = LocalNavigator.currentOrThrow
    val uriHandler = LocalUriHandler.current
    val currentLocale = Locale.getDefault()
    val collator = Collator.getInstance(currentLocale)
    val context = LocalContext.current
    val providers = remember(currentLocale) {
      Provider.values().sortedWith(compareBy(collator) { context.getString(it.title) })
    }

    val enabledLookupProviders by viewModel.enabledLookupProviders.asFlow()
      .collectAsStateWithLifecycle(initialValue = emptySet())

    val actionOpenInBrowser = stringResource(R.string.action_open_in_browser)

    SettingsScaffold(
      title = stringResource(R.string.settings_search),
      onNavigationClick = { navigator.pop() }
    ) {
      item {
        SettingsHeader(title = stringResource(R.string.pref_header_lookup_providers))
      }

      items(providers, key = { it.name }) { provider ->
        val checked =  provider.name in enabledLookupProviders

        SwitchPreference(
          modifier = Modifier.semantics {
            customActions = listOf(
              CustomAccessibilityAction(
                label = actionOpenInBrowser,
                action = {
                  runCatching { uriHandler.openUri(provider.url) }
                    .getOrNull() != null
                }
              )
            )
          },
          title = stringResource(provider.title),
          summary = provider.url,
          checked = checked,
          enabled = enabledLookupProviders.size > 1 || !checked,
          trailingContent = {
            IconButton(
              modifier = Modifier.clearAndSetSemantics {},
              onClick = {
                runCatching { uriHandler.openUri(provider.url) }
              }
            ) {
              Icon(
                imageVector = Icons.Outlined.OpenInBrowser,
                contentDescription = actionOpenInBrowser,
                tint = MaterialTheme.colorScheme.onSurface
              )
            }
          },
          onCheckedChange = { enabled ->
            val newSet = if (enabled) {
              enabledLookupProviders + provider.name
            } else {
              enabledLookupProviders - provider.name
            }

            viewModel.onEnabledLookupProvidersChanged(newSet)
          }
        )
      }
    }
  }

}