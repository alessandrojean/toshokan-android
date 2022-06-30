package io.github.alessandrojean.toshokan.presentation.ui.statistics.components

import android.icu.util.Currency
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.PeriodStatistics
import io.github.alessandrojean.toshokan.database.data.Statistics
import io.github.alessandrojean.toshokan.util.extension.formatToLocaleDate
import io.github.alessandrojean.toshokan.util.extension.isInCurrentMonth
import io.github.alessandrojean.toshokan.util.extension.isStartAndEndOfSameMonth
import io.github.alessandrojean.toshokan.util.extension.plus
import io.github.alessandrojean.toshokan.util.extension.toLocaleCurrencyString
import io.github.alessandrojean.toshokan.util.extension.toLocaleString
import io.github.alessandrojean.toshokan.util.extension.toLocalizedMonthYear

@Composable
fun StatisticsList(
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(),
  statistics: Statistics,
  periodStatistics: PeriodStatistics,
  currency: Currency,
  startPeriod: Long,
  endPeriod: Long,
  showValue: Boolean = false,
  onShowValueClick: () -> Unit,
  onChangePeriodClick: () -> Unit,
  onPeriodBoughtClick: () -> Unit,
  onPeriodReadClick: () -> Unit
) {
  val bookCount = remember(statistics.count) { statistics.count.toLocaleString() }
  val totalExpense = remember(statistics.total_expense) {
    (statistics.total_expense?.toFloat() ?: 0.0f).toLocaleCurrencyString(currency)
  }
  val totalSavings = remember(statistics.total_expense, statistics.real_total_expense) {
    val savings = ((statistics.real_total_expense ?: 0.0) - (statistics.total_expense ?: 0.0))
    savings.toFloat().coerceAtLeast(0.0f).toLocaleCurrencyString(currency)
  }
  val readingPercent = remember(statistics.count, statistics.read_count) {
    if (statistics.count == 0L) {
      "0"
    } else {
      val percent = statistics.read_count / statistics.count.toFloat() * 100f
      percent.toLocaleString { maximumFractionDigits = 1 }
    }
  }
  val periodExpense = remember(periodStatistics.total_expense) {
    (periodStatistics.total_expense?.toFloat() ?: 0.0f).toLocaleCurrencyString(currency)
  }
  val periodBoughts = remember(periodStatistics.bought_count) {
    periodStatistics.bought_count.toLocaleString()
  }
  val periodReads = remember(periodStatistics.read_count) {
    periodStatistics.read_count.toLocaleString()
  }

  val context = LocalContext.current
  val periodSubtitle = remember(context, startPeriod, endPeriod) {
    when {
      isInCurrentMonth(startPeriod) &&
        isInCurrentMonth(endPeriod) &&
        isStartAndEndOfSameMonth(startPeriod, endPeriod) -> {
        context.getString(
          R.string.period_current,
          startPeriod.toLocalizedMonthYear(context)
        )
      }
      isStartAndEndOfSameMonth(startPeriod, endPeriod) -> {
        startPeriod.toLocalizedMonthYear(context)
      }
      else -> {
        context.getString(
          R.string.period_custom,
          startPeriod.formatToLocaleDate(),
          endPeriod.formatToLocaleDate()
        )
      }
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .then(modifier),
    contentPadding = contentPadding + PaddingValues(vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    item {
      StatisticHeader(
        modifier = Modifier.padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
        title = stringResource(R.string.overview)
      )
    }
    item("book_count") {
      StatisticCard(
        contentPadding = PaddingValues(horizontal = 16.dp),
        title = stringResource(R.string.count),
        value = bookCount,
        icon = Icons.Outlined.Book
      )
    }
    item("reading_percent") {
      StatisticCard(
        contentPadding = PaddingValues(horizontal = 16.dp),
        title = stringResource(R.string.read),
        value = "$readingPercent%",
        icon = Icons.Outlined.Bookmarks
      )
    }
    item("total_expense") {
      StatisticCard(
        contentPadding = PaddingValues(horizontal = 16.dp),
        title = stringResource(R.string.total_expense),
        value = totalExpense,
        icon = Icons.Outlined.Paid,
        showValue = showValue,
        onClick = onShowValueClick
      )
    }
    item("total_savings") {
      StatisticCard(
        contentPadding = PaddingValues(horizontal = 16.dp),
        title = stringResource(R.string.total_savings),
        value = totalSavings,
        icon = Icons.Outlined.Savings,
        showValue = showValue,
        onClick = onShowValueClick
      )
    }
    item {
      StatisticHeader(
        modifier = Modifier
          .padding(vertical = 4.dp)
          .clickable { onChangePeriodClick() }
          .padding(horizontal = 16.dp, vertical = 4.dp),
        title = stringResource(R.string.by_period),
        subtitle = periodSubtitle,
        leadingAction = {
          IconButton(
            modifier = Modifier.semantics {},
            onClick = onChangePeriodClick
          ) {
            Icon(
              imageVector = Icons.Outlined.FilterList,
              contentDescription = stringResource(R.string.action_change_period)
            )
          }
        }
      )
    }
    item("period_expense") {
      StatisticCard(
        contentPadding = PaddingValues(horizontal = 16.dp),
        title = stringResource(R.string.period_expense),
        value = periodExpense,
        icon = Icons.Outlined.Paid,
        showValue = showValue,
        onClick = onShowValueClick
      )
    }
    item("period_bought") {
      StatisticCard(
        contentPadding = PaddingValues(horizontal = 16.dp),
        title = stringResource(R.string.bought_count),
        value = periodBoughts,
        icon = Icons.Outlined.LocalMall,
        onClick = onPeriodBoughtClick
      )
    }
    item("period_read") {
      StatisticCard(
        contentPadding = PaddingValues(horizontal = 16.dp),
        title = stringResource(R.string.read_count),
        value = periodReads,
        icon = Icons.Outlined.Bookmarks,
        onClick = onPeriodReadClick
      )
    }
  }
}