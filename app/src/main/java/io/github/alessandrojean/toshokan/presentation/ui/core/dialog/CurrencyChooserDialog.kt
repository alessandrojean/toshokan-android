package io.github.alessandrojean.toshokan.presentation.ui.core.dialog

import android.icu.util.Currency
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Divider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.ui.theme.DividerOpacity
import java.text.Collator
import java.util.Locale

@Composable
fun CurrencyChooserDialog(
  currency: Currency? = null,
  onChoose: (Currency) -> Unit,
  onClose: () -> Unit
) {
  val locale = Locale.getDefault()
  val collator = Collator.getInstance(locale)

  val currencies = remember {
    Currency
      .getAvailableCurrencies()
      .sortedWith(compareBy(collator) { it.getDisplayName(locale) })
  }

  var selected by remember { mutableStateOf(currency) }
  val listState = rememberLazyListState(
    initialFirstVisibleItemIndex = currencies.indexOfFirst {
      it.currencyCode == currency?.currencyCode
    }
  )

  Dialog(onDismissRequest = onClose) {
    Surface(
      modifier = Modifier.fillMaxHeight(0.85f),
      shape = MaterialTheme.shapes.extraLarge,
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          modifier = Modifier
            .fillMaxWidth()
            .padding(TitlePadding),
          maxLines = 1,
          text = stringResource(R.string.currency),
          color =  MaterialTheme.colorScheme.onSurface,
          style = MaterialTheme.typography.headlineSmall
        )
        Divider(
          modifier = Modifier.fillMaxWidth(),
          color = LocalContentColor.current.copy(alpha = DividerOpacity)
        )
        Box(
          modifier = Modifier
            .weight(weight = 1f, fill = false)
            .align(Alignment.Start)
        ) {
          LazyColumn(
            state = listState,
            modifier = Modifier
              .fillMaxWidth()
              .selectableGroup()
          ) {
            items(currencies, key = { it.currencyCode }) { currencyOption ->
              CurrencyOption(
                modifier = Modifier.fillMaxWidth(),
                currency = currencyOption,
                selected = selected?.currencyCode == currencyOption.currencyCode,
                onClick = { selected = currencyOption }
              )
            }
          }
        }
        Divider(
          modifier = Modifier.fillMaxWidth(),
          color = LocalContentColor.current.copy(alpha = DividerOpacity)
        )
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(ButtonBoxPadding)
        ) {
          Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
          ) {
            TextButton(onClick = onClose) {
              Text(stringResource(R.string.action_cancel))
            }
            TextButton(
              enabled = selected != null,
              onClick = {
                onChoose(selected!!)
                onClose()
              }
            ) {
              Text(stringResource(R.string.action_select))
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CurrencyOption(
  modifier: Modifier = Modifier,
  currency: Currency,
  selected: Boolean,
  onClick: () -> Unit
) {
  Row(
    modifier = modifier
      .selectable(
        selected = selected,
        onClick = onClick,
        role = Role.RadioButton
      )
      .padding(vertical = 16.dp, horizontal = 24.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    RadioButton(
      selected = selected,
      onClick = null
    )
    Text(
      modifier = Modifier.padding(start = 24.dp),
      text = currency.getDisplayName(Locale.getDefault()),
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

private val TitlePadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp)
private val ButtonBoxPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 18.dp)
