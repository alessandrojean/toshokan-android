package io.github.alessandrojean.toshokan.presentation.ui.statistics

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import io.github.alessandrojean.toshokan.data.preference.PreferencesManager
import io.github.alessandrojean.toshokan.database.data.PeriodStatistics
import io.github.alessandrojean.toshokan.database.data.Statistics
import io.github.alessandrojean.toshokan.repository.BooksRepository
import io.github.alessandrojean.toshokan.util.extension.firstDayOfCurrentMonth
import io.github.alessandrojean.toshokan.util.extension.lastDayOfCurrentMonth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
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

  val currency = preferencesManager.currency().get()

  var showValue by mutableStateOf(false)

  var startPeriod by mutableStateOf(firstDayOfCurrentMonth)
    private set
  var endPeriod by mutableStateOf(lastDayOfCurrentMonth)
    private set

  private var observeJob: Job? = null

  init {
    observeStatistics()
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

      finalFlow.collect { mutableState.value = it }
    }
  }

}