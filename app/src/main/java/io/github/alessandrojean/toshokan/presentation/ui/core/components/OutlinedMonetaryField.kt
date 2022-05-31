package io.github.alessandrojean.toshokan.presentation.ui.core.components

import android.icu.util.Currency
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.ui.core.dialog.FullScreenItemPickerDialog
import io.github.alessandrojean.toshokan.presentation.ui.core.dialog.ItemPickerDialog
import io.github.alessandrojean.toshokan.util.extension.parseLocaleValueOrNull
import java.text.Collator
import java.util.Locale

@Composable
fun OutlinedMonetaryField(
  modifier: Modifier = Modifier,
  value: String,
  currency: Currency,
  isError: Boolean = false,
  label: @Composable (() -> Unit)? = null,
  placeholder: @Composable (() -> Unit)? = null,
  onValueChange: (String) -> Unit,
  onCurrencyChange: (Currency) -> Unit,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default
) {
  val currentLocale = Locale.getDefault()
  var showCurrencyDialog by remember { mutableStateOf(false) }

  val collator = Collator.getInstance(currentLocale)

  val currencies = remember {
    Currency
      .getAvailableCurrencies()
      .sortedWith(compareBy(collator) { it.getDisplayName(currentLocale) })
  }

  FullScreenItemPickerDialog(
    visible = showCurrencyDialog,
    title = stringResource(R.string.currency),
    selected = currency,
    items = currencies,
    itemKey = { it.currencyCode },
    itemText = { it.getDisplayName(currentLocale) },
    onChoose = onCurrencyChange,
    onDismiss = { showCurrencyDialog = false },
    initialSearch = currency.currencyCode,
    search = { query, fullList ->
      fullList.filter {
        it.currencyCode.startsWith(query, ignoreCase = true) ||
          it.getDisplayName(currentLocale).contains(query, ignoreCase = true)
      }
    },
    searchPlaceholder = stringResource(R.string.currency_search_tip)
  )

  OutlinedTextField(
    modifier = modifier,
    value = value,
    label = label,
    placeholder = placeholder,
    isError = isError || value.parseLocaleValueOrNull() == null,
    leadingIcon = {
      TextButton(
        colors = ButtonDefaults.textButtonColors(
          contentColor = MaterialTheme.colorScheme.onSurface
        ),
        onClick = { showCurrencyDialog = true }
      ) {
        Text(currency.getSymbol(currentLocale))
      }
    },
    singleLine = true,
    keyboardOptions = keyboardOptions.copy(
      keyboardType = KeyboardType.Decimal
    ),
    keyboardActions = keyboardActions,
    textStyle = LocalTextStyle.current.copy(
      textAlign = TextAlign.Right
    ),
    onValueChange = onValueChange
  )
}