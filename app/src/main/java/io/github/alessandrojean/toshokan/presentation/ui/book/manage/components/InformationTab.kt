package io.github.alessandrojean.toshokan.presentation.ui.book.manage.components

import android.icu.util.Currency
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.Publisher
import io.github.alessandrojean.toshokan.database.data.Store
import io.github.alessandrojean.toshokan.domain.Price
import io.github.alessandrojean.toshokan.presentation.ui.core.components.OutlinedMonetaryField
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@Composable
fun InformationTab(
  code: String,
  title: String,
  synopsis: String,
  publisherText: String,
  allPublishers: List<Publisher>,
  labelPriceCurrency: Currency,
  labelPriceValue: String,
  paidPriceCurrency: Currency,
  paidPriceValue: String,
  onCodeChange: (String) -> Unit,
  onTitleChange: (String) -> Unit,
  onSynopsisChange: (String) -> Unit,
  onPublisherTextChange: (String) -> Unit,
  onPublisherChange: (Publisher) -> Unit,
  onLabelPriceValueChange: (String) -> Unit,
  onLabelPriceCurrencyChange: (Currency) -> Unit,
  onPaidPriceValueChange: (String) -> Unit,
  onPaidPriceCurrencyChange: (Currency) -> Unit
) {
  val scrollState = rememberScrollState()
  var publisherExpanded by remember { mutableStateOf(false) }
  val focusManager = LocalFocusManager.current
  val keyboardController = LocalSoftwareKeyboardController.current

  // TODO: Change to LazyColumn when the focus issue gets fixed.
  // Ref: https://issuetracker.google.com/issues/179203700
  // TODO: Handle focus on text fields when scroll.
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top)
  ) {
    OutlinedTextField(
      modifier = Modifier.fillMaxWidth(),
      value = code,
      onValueChange = onCodeChange,
      singleLine = true,
      isError = code.isEmpty(),
      label = { Text(stringResource(R.string.code)) },
      keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
      keyboardActions = KeyboardActions(
        onNext = {
          focusManager.moveFocus(FocusDirection.Down)
        }
      )
    )

    OutlinedTextField(
      modifier = Modifier.fillMaxWidth(),
      value = title,
      onValueChange = onTitleChange,
      maxLines = 3,
      isError = title.isEmpty(),
      label = { Text(stringResource(R.string.title)) },
      keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
      keyboardActions = KeyboardActions(
        onNext = {
          focusManager.moveFocus(FocusDirection.Down)
        }
      )
    )

    ExposedDropdownMenuBox(
      expanded = publisherExpanded,
      onExpandedChange = { publisherExpanded = it }
    ) {
      val filteringOptions = allPublishers.filter { it.name.contains(publisherText, true) }

      OutlinedTextField(
        modifier = Modifier
          .fillMaxWidth()
          .onFocusChanged { publisherExpanded = it.isFocused },
        value = publisherText,
        onValueChange = onPublisherTextChange,
        singleLine = true,
        label = { Text(stringResource(R.string.publisher)) },
        isError = publisherText.isBlank(),
        trailingIcon = {
          if (filteringOptions.isNotEmpty()) {
            ExposedDropdownMenuDefaults.TrailingIcon(publisherExpanded)
          }
        },
        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(
          onNext = {
            focusManager.moveFocus(FocusDirection.Down)
          }
        )
      )

      if (filteringOptions.isNotEmpty()) {
        ExposedDropdownMenu(
          modifier = Modifier.exposedDropdownSize(),
          expanded = publisherExpanded,
          onDismissRequest = { publisherExpanded = false }
        ) {
          filteringOptions.forEach { selectionOption ->
            DropdownMenuItem(
              text = { Text(selectionOption.name) },
              onClick = {
                onPublisherTextChange(selectionOption.name)
                onPublisherChange(selectionOption)
                publisherExpanded = false
              }
            )
          }
        }
      }
    }

    OutlinedTextField(
      modifier = Modifier.fillMaxWidth(),
      value = synopsis,
      onValueChange = onSynopsisChange,
      maxLines = 10,
      label = { Text(stringResource(R.string.synopsis)) }
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      OutlinedMonetaryField(
        modifier = Modifier.weight(0.5f),
        value = labelPriceValue,
        currency = labelPriceCurrency,
        isError = labelPriceValue.isEmpty(),
        label = { Text(stringResource(R.string.label_price)) },
        onValueChange = onLabelPriceValueChange,
        onCurrencyChange = onLabelPriceCurrencyChange,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(
          onNext = {
            focusManager.moveFocus(FocusDirection.Right)
          }
        )
      )
      OutlinedMonetaryField(
        modifier = Modifier.weight(0.5f),
        value = paidPriceValue,
        currency = paidPriceCurrency,
        isError = paidPriceValue.isEmpty(),
        label = { Text(stringResource(R.string.paid_price)) },
        onValueChange = onPaidPriceValueChange,
        onCurrencyChange = onPaidPriceCurrencyChange,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
          onDone = {
            focusManager.clearFocus()
            keyboardController?.hide()
          }
        )
      )
    }
  }
}