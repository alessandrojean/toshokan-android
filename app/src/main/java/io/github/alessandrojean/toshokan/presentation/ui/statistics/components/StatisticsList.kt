package io.github.alessandrojean.toshokan.presentation.ui.statistics.components

import android.icu.util.Currency
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.patrykandpatryk.vico.core.entry.ChartEntryModelProducer
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.PeriodStatistics
import io.github.alessandrojean.toshokan.database.data.Statistics
import io.github.alessandrojean.toshokan.presentation.ui.statistics.ranking.RankingScreen
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
  monthlyExpenseProducer: ChartEntryModelProducer,
  monthlyBoughtsAndReadsProducer: ChartEntryModelProducer,
  currency: Currency,
  startPeriod: Long,
  endPeriod: Long,
  showValue: Boolean = false,
  onShowValueClick: () -> Unit,
  onChangePeriodClick: () -> Unit,
  onPeriodBoughtClick: () -> Unit,
  onPeriodReadClick: () -> Unit,
  onRankingClick: (RankingScreen.RankingType) -> Unit
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
  val (pagesRead, periodPagesRead) = remember(statistics.pages_read, periodStatistics.pages_read) {
    statistics.pages_read?.toInt()?.toLocaleString() to
      periodStatistics.pages_read?.toInt()?.toLocaleString()
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
    contentPadding = contentPadding + PaddingValues(top = 8.dp, bottom = 16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    val horizontalPadding = PaddingValues(horizontal = 16.dp)

    item {
      StatisticHeader(
        modifier = Modifier.padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
        title = stringResource(R.string.overview)
      )
    }
    item("book_count") {
      StatisticCard(
        contentPadding = horizontalPadding,
        title = stringResource(R.string.count),
        value = bookCount,
        icon = Icons.Outlined.Book
      )
    }
    item("reading_percent") {
      StatisticCard(
        contentPadding = horizontalPadding,
        title = stringResource(R.string.read),
        value = "$readingPercent%",
        icon = Icons.Outlined.Bookmarks
      )
    }
    item("pages_read") {
      StatisticCard(
        contentPadding = horizontalPadding,
        title = stringResource(R.string.pages_read),
        value = pagesRead ?: "0",
        icon = Icons.Outlined.MenuBook
      )
    }
    item("total_expense") {
      StatisticCard(
        contentPadding = horizontalPadding,
        title = stringResource(R.string.total_expense),
        value = totalExpense,
        icon = Icons.Outlined.Paid,
        showValue = showValue,
        onClick = onShowValueClick
      )
    }
    item("total_savings") {
      StatisticCard(
        contentPadding = horizontalPadding,
        title = stringResource(R.string.total_savings),
        value = totalSavings,
        icon = Icons.Outlined.Savings,
        showValue = showValue,
        onClick = onShowValueClick
      )
    }
    item {
      StatisticHeader(
        modifier = Modifier.padding(bottom = 8.dp, top = 12.dp, start = 16.dp, end = 16.dp),
        title = stringResource(R.string.charts)
      )
    }
    item("monthly_expense_chart") {
      MonthlyExpenseChart(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        chartModelProducer = monthlyExpenseProducer,
        currency = currency
      )
    }
    item("monthly_boughts_and_reads_chart") {
      MonthlyBoughtsAndReadsChart(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        chartModelProducer = monthlyBoughtsAndReadsProducer,
        currency = currency
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
        contentPadding = horizontalPadding,
        title = stringResource(R.string.period_expense),
        value = periodExpense,
        icon = Icons.Outlined.Paid,
        showValue = showValue,
        onClick = onShowValueClick
      )
    }
    item("period_bought") {
      StatisticCard(
        contentPadding = horizontalPadding,
        title = stringResource(R.string.bought_count),
        value = periodBoughts,
        icon = Icons.Outlined.LocalMall,
        onClick = onPeriodBoughtClick
      )
    }
    item("period_read") {
      StatisticCard(
        contentPadding = horizontalPadding,
        title = stringResource(R.string.read_count),
        value = periodReads,
        icon = Icons.Outlined.Bookmarks,
        onClick = onPeriodReadClick
      )
    }
    item("period_pages_read") {
      StatisticCard(
        contentPadding = horizontalPadding,
        title = stringResource(R.string.pages_read),
        value = periodPagesRead ?: "0",
        icon = Icons.Outlined.MenuBook
      )
    }
    item {
      StatisticHeader(
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
        title = stringResource(R.string.rankings)
      )
    }
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        RankingCard(
          modifier = Modifier.weight(1f),
          title = stringResource(R.string.authors),
          icon = rememberVectorPainter(Icons.Outlined.Group),
          onClick = { onRankingClick(RankingScreen.RankingType.AUTHOR) }
        )
        RankingCard(
          modifier = Modifier.weight(1f),
          title = stringResource(R.string.publishers),
          icon = rememberVectorPainter(Icons.Outlined.Domain),
          onClick = { onRankingClick(RankingScreen.RankingType.PUBLISHER) }
        )
        RankingCard(
          modifier = Modifier.weight(1f),
          title = stringResource(R.string.stores),
          icon = rememberVectorPainter(Icons.Outlined.LocalMall),
          onClick = { onRankingClick(RankingScreen.RankingType.STORE) }
        )
      }
    }
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        RankingCard(
          modifier = Modifier.weight(1f),
          title = stringResource(R.string.filter_collection),
          icon = rememberVectorPainter(Icons.Outlined.CollectionsBookmark),
          onClick = { onRankingClick(RankingScreen.RankingType.COLLECTION) }
        )
        RankingCard(
          modifier = Modifier.weight(1f),
          title = stringResource(R.string.groups),
          icon = rememberVectorPainter(Icons.Outlined.GroupWork),
          onClick = { onRankingClick(RankingScreen.RankingType.GROUP) }
        )
        RankingCard(
          modifier = Modifier.weight(1f),
          title = stringResource(R.string.tags),
          icon = rememberVectorPainter(Icons.Outlined.Label),
          onClick = { onRankingClick(RankingScreen.RankingType.TAG) }
        )
      }
    }
  }
}