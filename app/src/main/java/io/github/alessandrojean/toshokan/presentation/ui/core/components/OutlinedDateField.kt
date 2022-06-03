package io.github.alessandrojean.toshokan.presentation.ui.core.components

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.util.extension.formatToLocaleDate
import io.github.alessandrojean.toshokan.util.extension.toLocalCalendar

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
  val activity = LocalContext.current as AppCompatActivity

  OutlinedTextField(
    modifier = modifier
      .onFocusChanged {
        if (it.isFocused) {
          showDatePicker(
            activity = activity,
            date = value,
            titleText = dialogTitle,
            onDateChoose = onValueChange
          )
        }
      },
    readOnly = true,
    value = value?.formatToLocaleDate().orEmpty(),
    isError = isError,
    label = label,
    placeholder = placeholder,
    onValueChange = {},
    trailingIcon = {
      IconButton(
        onClick = {
          showDatePicker(
            activity = activity,
            date = value,
            titleText = dialogTitle,
            onDateChoose = onValueChange
          )
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

private fun showDatePicker(
  activity: AppCompatActivity,
  titleText: String,
  date: Long?,
  onDateChoose: (Long?) -> Unit
) {
  val constraints = CalendarConstraints.Builder()
    .setValidator(DateValidatorPointBackward.now())
    .build()

  val picker = MaterialDatePicker.Builder.datePicker()
    .setTitleText(titleText)
    .setSelection(date ?: MaterialDatePicker.todayInUtcMilliseconds())
    .setCalendarConstraints(constraints)
    .build()

  picker.addOnPositiveButtonClickListener { onDateChoose(it.toLocalCalendar()?.timeInMillis) }
  picker.show(activity.supportFragmentManager, picker.toString())
}