package io.github.alessandrojean.toshokan.presentation.ui.book.manage.components

import android.icu.util.Currency
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ManageSearch
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.Publisher
import io.github.alessandrojean.toshokan.presentation.ui.core.components.OutlinedMonetaryField
import io.github.alessandrojean.toshokan.presentation.ui.core.dialog.FullScreenItemPickerDialog
import io.github.alessandrojean.toshokan.util.extension.bringIntoViewOnFocus
import io.github.alessandrojean.toshokan.util.extension.navigationBarsWithImePadding
import io.github.alessandrojean.toshokan.util.extension.parseLocaleValueOrNull

@Composable
fun InformationTab(
  code: String,
  title: String,
  synopsis: String,
  publisher: Publisher?,
  publisherText: String,
  allPublishers: List<Publisher>,
  pageCountText: String,
  labelPriceCurrency: Currency,
  labelPriceValue: String,
  paidPriceCurrency: Currency,
  paidPriceValue: String,
  dimensionWidth: String,
  dimensionHeight: String,
  onCodeChange: (String) -> Unit,
  onTitleChange: (String) -> Unit,
  onSynopsisChange: (String) -> Unit,
  onPublisherTextChange: (String) -> Unit,
  onPublisherChange: (Publisher?) -> Unit,
  onPageCountTextChange: (String) -> Unit,
  onLabelPriceValueChange: (String) -> Unit,
  onLabelPriceCurrencyChange: (Currency) -> Unit,
  onPaidPriceValueChange: (String) -> Unit,
  onPaidPriceCurrencyChange: (Currency) -> Unit,
  onDimensionWidthChange: (String) -> Unit,
  onDimensionHeightChange: (String) -> Unit,
) {
  val scrollState = rememberScrollState()
  var showPublisherPickerDialog by remember { mutableStateOf(false) }
  val focusManager = LocalFocusManager.current
  val keyboardController = LocalSoftwareKeyboardController.current
  val scope = rememberCoroutineScope()

  FullScreenItemPickerDialog(
    visible = showPublisherPickerDialog,
    title = stringResource(R.string.publishers),
    selected = publisher,
    initialSearch = publisherText,
    items = allPublishers,
    itemKey = { it.id },
    itemText = { it.name },
    onChoose = {
      onPublisherChange.invoke(it)
      onPublisherTextChange.invoke(it.name)
    },
    onDismiss = { showPublisherPickerDialog = false }
  )

  // TODO: Change to LazyColumn when the focus issue gets fixed.
  // Ref: https://issuetracker.google.com/issues/179203700
  // TODO: Handle focus on text fields when scroll.
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(12.dp)
      .navigationBarsWithImePadding()
      .verticalScroll(scrollState),
    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top)
  ) {
    OutlinedTextField(
      modifier = Modifier
        .fillMaxWidth()
        .bringIntoViewOnFocus(scope),
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
      modifier = Modifier
        .fillMaxWidth()
        .bringIntoViewOnFocus(scope),
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

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        modifier = Modifier
          .weight(1f)
          .bringIntoViewOnFocus(scope),
        value = publisherText,
        onValueChange = {
          onPublisherTextChange.invoke(it)
          onPublisherChange.invoke(null)
        },
        singleLine = true,
        label = { Text(stringResource(R.string.publisher)) },
        isError = publisherText.isBlank(),
        trailingIcon = {
          IconButton(onClick = { showPublisherPickerDialog = true }) {
            Icon(
              imageVector = Icons.Outlined.ManageSearch,
              contentDescription = stringResource(R.string.action_search)
            )
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

      OutlinedTextField(
        modifier = Modifier
          .weight(1f)
          .bringIntoViewOnFocus(scope),
        value = pageCountText,
        onValueChange = onPageCountTextChange,
        singleLine = true,
        isError = pageCountText.toIntOrNull() == null,
        label = { Text(stringResource(R.string.page_count)) },
        keyboardOptions = KeyboardOptions.Default.copy(
          imeAction = ImeAction.Next,
          keyboardType = KeyboardType.Number
        ),
        keyboardActions = KeyboardActions(
          onNext = {
            focusManager.moveFocus(FocusDirection.Down)
          }
        )
      )
    }

    OutlinedTextField(
      modifier = Modifier
        .fillMaxWidth()
        .bringIntoViewOnFocus(scope),
      value = synopsis,
      onValueChange = onSynopsisChange,
      maxLines = 10,
      label = { Text(stringResource(R.string.synopsis)) }
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        modifier = Modifier
          .weight(1f)
          .bringIntoViewOnFocus(scope),
        value = dimensionWidth,
        isError = dimensionWidth.isEmpty() || dimensionWidth.parseLocaleValueOrNull() == null,
        label = { Text(stringResource(R.string.width_cm)) },
        trailingIcon = { Text("cm", style = MaterialTheme.typography.bodySmall) },
        onValueChange = onDimensionWidthChange,
        keyboardOptions = KeyboardOptions.Default.copy(
          keyboardType = KeyboardType.Decimal,
          imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
          onNext = {
            focusManager.moveFocus(FocusDirection.Right)
          }
        )
      )
      Text("×", modifier = Modifier.padding(top = 4.dp))
      OutlinedTextField(
        modifier = Modifier
          .weight(1f)
          .bringIntoViewOnFocus(scope),
        value = dimensionHeight,
        isError = dimensionHeight.isEmpty() || dimensionHeight.parseLocaleValueOrNull() == null,
        label = { Text(stringResource(R.string.height_cm)) },
        trailingIcon = { Text("cm", style = MaterialTheme.typography.bodySmall) },
        onValueChange = onDimensionHeightChange,
        keyboardOptions = KeyboardOptions.Default.copy(
          keyboardType = KeyboardType.Decimal,
          imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
          onNext = {
            focusManager.moveFocus(FocusDirection.Next)
          }
        )
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      OutlinedMonetaryField(
        modifier = Modifier
          .weight(0.5f)
          .bringIntoViewOnFocus(scope),
        value = labelPriceValue,
        currency = labelPriceCurrency,
        isError = labelPriceValue.isEmpty(),
        label = {
          Text(
            text = stringResource(R.string.label_price),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        },
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
        modifier = Modifier
          .weight(0.5f)
          .bringIntoViewOnFocus(scope),
        value = paidPriceValue,
        currency = paidPriceCurrency,
        isError = paidPriceValue.isEmpty(),
        label = {
          Text(
            text = stringResource(R.string.paid_price),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        },
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
