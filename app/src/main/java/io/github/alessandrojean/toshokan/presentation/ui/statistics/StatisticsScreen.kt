package io.github.alessandrojean.toshokan.presentation.ui.statistics

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.android.material.datepicker.DateValidatorPointBackward
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.domain.DateRange
import io.github.alessandrojean.toshokan.domain.SearchFilters
import io.github.alessandrojean.toshokan.presentation.ui.core.components.BoxedCircularProgressIndicator
import io.github.alessandrojean.toshokan.presentation.ui.core.components.EnhancedSmallTopAppBar
import io.github.alessandrojean.toshokan.presentation.ui.core.components.NoItemsFound
import io.github.alessandrojean.toshokan.presentation.ui.core.picker.showDateRangePicker
import io.github.alessandrojean.toshokan.presentation.ui.search.SearchScreen
import io.github.alessandrojean.toshokan.presentation.ui.statistics.components.StatisticsList
import io.github.alessandrojean.toshokan.util.extension.collectAsStateWithLifecycle
import io.github.alessandrojean.toshokan.util.extension.lastDayOfCurrentMonth
import io.github.alessandrojean.toshokan.util.extension.push
import io.github.alessandrojean.toshokan.util.extension.toLocalEpochMilli

class StatisticsScreen : AndroidScreen() {

  override val key = "statistics_screen"

  @Composable
  override fun Content() {
    val screenModel = getScreenModel<StatisticsScreenModel>()
    val state by screenModel.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current as AppCompatActivity
    val navigator = LocalNavigator.currentOrThrow

    Scaffold(
      topBar = {
        EnhancedSmallTopAppBar(
          contentPadding = WindowInsets.statusBars.asPaddingValues(),
          title = { Text(stringResource(R.string.statistics)) },
          actions = {
            IconToggleButton(
              modifier = Modifier.semantics {},
              checked = screenModel.showValue,
              onCheckedChange = { screenModel.showValue = it },
              colors = IconButtonDefaults.iconToggleButtonColors(
                contentColor = LocalContentColor.current,
                checkedContentColor = LocalContentColor.current
              )
            ) {
              Icon(
                imageVector = if (screenModel.showValue) {
                  Icons.Outlined.Visibility
                } else {
                  Icons.Outlined.VisibilityOff
                },
                contentDescription = if (screenModel.showValue) {
                  stringResource(R.string.action_hide_value)
                } else {
                  stringResource(R.string.action_show_value)
                }
              )
            }
          }
        )
      },
      content = { innerPadding ->
        Crossfade(state) { state ->
          when (state) {
            StatisticsScreenModel.State.Empty -> {
              NoItemsFound(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(innerPadding),
                icon = Icons.Outlined.Insights
              )
            }
            StatisticsScreenModel.State.Loading -> {
              BoxedCircularProgressIndicator(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(innerPadding)
              )
            }
            is StatisticsScreenModel.State.Result -> {
              StatisticsList(
                modifier = Modifier.fillMaxSize(),
                contentPadding = innerPadding,
                statistics = state.statistics,
                periodStatistics = state.periodStatistics,
                currency = screenModel.currency,
                startPeriod = screenModel.startPeriod,
                endPeriod = screenModel.endPeriod,
                showValue = screenModel.showValue,
                onShowValueClick = { screenModel.showValue = !screenModel.showValue },
                onChangePeriodClick = {
                  showDateRangePicker(
                    activity = activity,
                    titleText = activity.getString(R.string.by_period),
                    builderBlock = {
                      val lastDay = lastDayOfCurrentMonth.toLocalEpochMilli()
                      val validator = DateValidatorPointBackward.before(lastDay)
                      setValidator(validator)
                    },
                    range = DateRange(screenModel.startPeriod, screenModel.endPeriod),
                    onRangeChoose = { period ->
                      period?.let { screenModel.onPeriodChange(it.start, it.end) }
                    }
                  )
                },
                onPeriodBoughtClick = {
                  navigator.push {
                    SearchScreen(
                      filters = SearchFilters.Incomplete(
                        boughtAt = DateRange(screenModel.startPeriod, screenModel.endPeriod)
                      )
                    )
                  }
                },
                onPeriodReadClick = {
                  navigator.push {
                    SearchScreen(
                      filters = SearchFilters.Incomplete(
                        readAt = DateRange(screenModel.startPeriod, screenModel.endPeriod)
                      )
                    )
                  }
                }
              )
            }
          }
        }
      }
    )
  }

}
