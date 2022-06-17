package io.github.alessandrojean.toshokan.presentation.ui.core.picker

import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.domain.DateRange
import io.github.alessandrojean.toshokan.util.extension.toLocalCalendar

fun showDatePicker(
  activity: AppCompatActivity,
  titleText: String,
  date: Long? = null,
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

fun showDateRangePicker(
  activity: AppCompatActivity,
  titleText: String,
  range: DateRange? = null,
  onRangeChoose: (DateRange?) -> Unit
) {
  val constraints = CalendarConstraints.Builder()
    .setValidator(DateValidatorPointBackward.now())
    .build()

  val today = MaterialDatePicker.todayInUtcMilliseconds()

  val picker = MaterialDatePicker.Builder.dateRangePicker()
    .setTitleText(titleText)
    .setSelection(range?.toSelection() ?: Pair(today, today))
    .setCalendarConstraints(constraints)
    .setNegativeButtonText(R.string.action_clear)
    .build()

  picker.addOnPositiveButtonClickListener {
    onRangeChoose(DateRange.fromSelection(it))
  }
  picker.addOnNegativeButtonClickListener {
    onRangeChoose(null)
  }
  picker.show(activity.supportFragmentManager, picker.toString())
}