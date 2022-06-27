package io.github.alessandrojean.toshokan.presentation.ui.core.components

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.ui.core.picker.showDatePicker
import io.github.alessandrojean.toshokan.util.extension.formatToLocaleDate
import io.github.alessandrojean.toshokan.util.extension.toLocalEpochMilli

@Composable
fun OutlinedDateField(
  modifier: Modifier = Modifier,
  value: Long?,
  isError: Boolean = false,
  dialogTitle: String,
  label: @Composable (() -> Unit)? = null,
  placeholder: @Composable (() -> Unit)? = null,
  onValueChange: (Long?) -> Unit,
) {
  val activity = LocalContext.current as? AppCompatActivity

  val localValue = remember(value) {
    value?.formatToLocaleDate().orEmpty()
  }

  OutlinedTextField(
    modifier = modifier
      .onFocusChanged {
        if (it.isFocused && activity != null) {
          showDatePicker(
            activity = activity,
            date = value,
            titleText = dialogTitle,
            onDateChoose = onValueChange
          )
        }
      },
    readOnly = true,
    value = localValue,
    isError = isError,
    label = label,
    placeholder = placeholder,
    onValueChange = {},
    trailingIcon = {
      IconButton(
        onClick = {
          activity?.let {
            showDatePicker(
              activity = it,
              date = value,
              titleText = dialogTitle,
              onDateChoose = onValueChange
            )
          }
        }
      ) {
        Icon(
          imageVector = Icons.Outlined.CalendarMonth,
          contentDescription = stringResource(R.string.action_pick_date)
        )
      }
    }
  )
}
