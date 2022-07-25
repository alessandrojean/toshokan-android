package io.github.alessandrojean.toshokan.presentation.ui.statistics

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import com.patrykandpatryk.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatryk.vico.core.entry.FloatEntry
import io.github.alessandrojean.toshokan.data.preference.PreferencesManager
import io.github.alessandrojean.toshokan.database.data.PeriodStatistics
import io.github.alessandrojean.toshokan.database.data.Statistics
import io.github.alessandrojean.toshokan.repository.BooksRepository
import io.github.alessandrojean.toshokan.util.extension.firstDayOfCurrentMonth
import io.github.alessandrojean.toshokan.util.extension.lastDayOfCurrentMonth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import logcat.logcat
import java.time.Month
import java.time.Year
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

class StatisticsScreenModel @Inject constructor(
  private val booksRepository: BooksRepository,
  preferencesManager: PreferencesManager
): StateScreenModel<StatisticsScreenModel.State>(State.Empty) {

  sealed class State {
    object Empty : State()
    object Loading : State()
    data class Result(
      val statistics: Statistics,
      val periodStatistics: PeriodStatistics
    ) : State()
  }

  val currency = preferencesManager.currency().getObject()

  var showValue by mutableStateOf(false)
    private set

  var startPeriod by mutableStateOf(firstDayOfCurrentMonth)
    private set
  var endPeriod by mutableStateOf(lastDayOfCurrentMonth)
    private set

  private var observeJob: Job? = null

  val monthlyExpenseModelProducer = ChartEntryModelProducer()
  val monthlyBoughtsAndReadsModelProducer = ChartEntryModelProducer()

  init {
    observeStatistics()
    observeMonthlyExpense()
    observeMonthlyBoughtsAndReads()
  }

  fun onPeriodChange(start: Long, end: Long) {
    startPeriod = start
    endPeriod = end
    observeStatistics()
  }

  private fun observeStatistics() {
    observeJob?.cancel()

    observeJob = coroutineScope.launch {
      if (mutableState.value == State.Empty) {
        mutableState.value = State.Loading
      }

      val statisticsFlow = booksRepository.subscribeToStatistics(currency)
      val periodStatisticsFlow = booksRepository.subscribeToPeriodStatistics(
        currency = currency,
        start = startPeriod,
        end = endPeriod
      )

      val finalFlow = combine(statisticsFlow, periodStatisticsFlow) { statistics, periodStatistics ->
        if (statistics.count == 0L) {
          State.Empty
        } else {
          State.Result(statistics, periodStatistics)
        }
      }

      finalFlow.collectLatest { mutableState.value = it }
    }
  }

  private fun observeMonthlyExpense() = coroutineScope.launch {
    booksRepository.subscribeToMonthlyExpense(currency, Year.now())
      .collectLatest { monthlyExpense ->
        monthlyExpenseModelProducer.setEntries(monthlyExpense.toFloatEntries())
      }
  }

  private fun observeMonthlyBoughtsAndReads() = coroutineScope.launch {
    val monthlyBoughtsFlow = booksRepository.subscribeToMonthlyBoughts(Year.now())
    val monthlyReadsFlow = booksRepository.subscribeToMonthlyReads(Year.now())

    val finalFlow = combine(monthlyBoughtsFlow, monthlyReadsFlow) { monthlyBoughts, monthlyReads ->
      listOfNotNull(
        monthlyBoughts.toFloatEntries().takeIf { it.isNotEmpty() },
        monthlyReads.toFloatEntries().takeIf { it.isNotEmpty() }
      )
    }

    finalFlow.collectLatest { monthlyBoughtsAndReads ->
      monthlyBoughtsAndReadsModelProducer.setEntries(monthlyBoughtsAndReads)
    }
  }

  private fun Map<Month, Float>.toFloatEntries(): List<FloatEntry> = map { (month, expense) ->
    FloatEntry(x = month.value.toFloat(), y = expense)
  }

  @JvmName("toFloatEntriesFromInt")
  private fun Map<Month, Int>.toFloatEntries(): List<FloatEntry> = map { (month, count) ->
    FloatEntry(x = month.value.toFloat(), y = count.toFloat())
  }

  fun toggleShowValue() {
    showValue = !showValue
  }

}