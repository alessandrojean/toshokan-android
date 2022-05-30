package io.github.alessandrojean.toshokan.presentation.ui.core.components

import android.icu.util.Currency
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import io.github.alessandrojean.toshokan.presentation.ui.core.dialog.CurrencyChooserDialog
import io.github.alessandrojean.toshokan.util.extension.parseLocaleValueOrNull
import java.text.NumberFormat
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

  if (showCurrencyDialog) {
    CurrencyChooserDialog(
      currency = currency,
      onChoose = onCurrencyChange,
      onClose = { showCurrencyDialog = false }
    )
  }

  OutlinedTextField(
    modifier = modifier,
    value = value,
    label = label,
    placeholder = placeholder,
    isError = isError || value.parseLocaleValueOrNull() == null,
    leadingIcon = {
      TextButton(onClick = { showCurrencyDialog = true }) {
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