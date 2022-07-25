package io.github.alessandrojean.toshokan.presentation.ui.statistics.components

import android.graphics.Typeface
import android.icu.util.Currency
import android.text.TextUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatryk.vico.compose.axis.axisLabelComponent
import com.patrykandpatryk.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatryk.vico.compose.axis.vertical.startAxis
import com.patrykandpatryk.vico.compose.chart.Chart
import com.patrykandpatryk.vico.compose.chart.column.columnChart
import com.patrykandpatryk.vico.compose.chart.line.lineChart
import com.patrykandpatryk.vico.compose.component.shapeComponent
import com.patrykandpatryk.vico.compose.legend.verticalLegend
import com.patrykandpatryk.vico.compose.legend.verticalLegendItem
import com.patrykandpatryk.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatryk.vico.compose.style.ChartStyle
import com.patrykandpatryk.vico.compose.style.ProvideChartStyle
import com.patrykandpatryk.vico.compose.style.currentChartStyle
import com.patrykandpatryk.vico.core.axis.AxisPosition
import com.patrykandpatryk.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatryk.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatryk.vico.core.chart.values.ChartValues
import com.patrykandpatryk.vico.core.component.shape.Shapes
import com.patrykandpatryk.vico.core.component.text.textComponent
import com.patrykandpatryk.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatryk.vico.core.extension.floor
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.util.extension.toLocaleCurrencyString
import java.time.Month
import java.time.Year
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ChartCard(
  modifier: Modifier = Modifier,
  title: String,
  chartStyle: ChartStyle = m3ChartStyle(),
  legend: @Composable (() -> Unit)? = null,
  chart: @Composable () -> Unit
) {
  Surface(
    modifier = modifier,
    tonalElevation = 6.dp,
    shape = MaterialTheme.shapes.large
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium
      )
      ProvideChartStyle(
        chartStyle = chartStyle,
        content = chart
      )
      legend?.invoke()
    }
  }
}

@Composable
fun Legend(
  modifier: Modifier = Modifier,
  content: @Composable RowScope.() -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .then(modifier),
    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
    content = content
  )
}

@Composable
fun LegendItem(
  modifier: Modifier = Modifier,
  color: Color,
  text: String
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(12.dp)
        .clip(MaterialTheme.shapes.small)
        .background(color)
    )
    Text(
      text = text,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
fun MonthlyExpenseChart(
  modifier: Modifier = Modifier,
  chartModelProducer: ChartEntryModelProducer,
  chartStyle: ChartStyle = m3ChartStyle(),
  currency: Currency,
  stepY: Int = 300
) {
  ChartCard(
    modifier = modifier,
    title = stringResource(R.string.monthly_expense),
    chartStyle = chartStyle
  ) {
    val model = chartModelProducer.getModel()
    val minY = ((model.minY.toInt() / stepY) * stepY).coerceAtLeast(0)
    val maxY = ((model.maxY.toInt() / stepY) + 1) * stepY
    val maxX = remember { YearMonth.now().monthValue.toFloat() }

    Chart(
      isZoomEnabled = false,
      chart = lineChart(
        minY = minY.toFloat(),
        maxY = maxY.toFloat(),
        maxX = maxX
      ),
      chartModelProducer = chartModelProducer,
      startAxis = startAxis(
        label = axisLabelComponent(
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textSize = 10.sp,
          typeface = Typeface.DEFAULT
        ),
        maxLabelCount = (maxY - minY) / stepY,
        valueFormatter = remember { CurrencyFormatter(currency) }
      ),
      bottomAxis = bottomAxis(
        label = axisLabelComponent(
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textSize = 12.sp,
          typeface = Typeface.DEFAULT,
          lineCount = 1,
          ellipsize = TextUtils.TruncateAt.END
        ),
        valueFormatter = remember { ShortMonthFormatter() },
        guideline = null,
      ),
    )
  }
}

@Composable
fun MonthlyBoughtsAndReadsChart(
  modifier: Modifier = Modifier,
  chartModelProducer: ChartEntryModelProducer,
  chartStyle: ChartStyle = m3ChartStyle(),
  currency: Currency,
  stepY: Int = 20
) {
  ChartCard(
    modifier = modifier,
    title = stringResource(R.string.monthly_boughts_and_reads),
    chartStyle = chartStyle,
    legend = {
      Legend {
        LegendItem(
          color = MaterialTheme.colorScheme.primary,
          text = stringResource(R.string.bought_count)
        )
        LegendItem(
          color = MaterialTheme.colorScheme.secondary,
          text = stringResource(R.string.read_count)
        )
      }
    }
  ) {
    val model = chartModelProducer.getModel()
    val minY = ((model.minY.toInt() / stepY) * stepY).coerceAtLeast(0)
    val maxY = ((model.maxY.toInt() / stepY) + 1) * stepY
    val maxX = remember { YearMonth.now().monthValue.toFloat() }

    Chart(
      isZoomEnabled = false,
      chart = columnChart(
        minY = minY.toFloat(),
        maxY = maxY.toFloat(),
        maxX = maxX
      ),
      chartModelProducer = chartModelProducer,
      startAxis = startAxis(
        label = axisLabelComponent(
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textSize = 10.sp,
          typeface = Typeface.DEFAULT
        ),
        maxLabelCount = (maxY - minY) / stepY,
      ),
      bottomAxis = bottomAxis(
        label = axisLabelComponent(
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textSize = 12.sp,
          typeface = Typeface.DEFAULT,
          lineCount = 1,
          ellipsize = TextUtils.TruncateAt.END
        ),
        valueFormatter = remember { ShortMonthFormatter() },
        guideline = null,
      ),
    )
  }
}

class ShortMonthFormatter<P : AxisPosition>(
  val style: TextStyle = TextStyle.SHORT,
  val locale: Locale = Locale.getDefault()
) : AxisValueFormatter<P> {
  override fun formatValue(value: Float, chartValues: ChartValues): CharSequence {
    val monthValue = value.floor.toInt()

    if (monthValue < 1f || monthValue > 12f) {
      return ""
    }

    return Month
      .of(monthValue)
      .getDisplayName(style, locale)
  }
}

class CurrencyFormatter<P : AxisPosition>(
  val currency: Currency,
  val locale: Locale = Locale.getDefault()
) : AxisValueFormatter<P> {
  override fun formatValue(value: Float, chartValues: ChartValues): CharSequence {
    return value.toLocaleCurrencyString(
      currency = currency,
      locale = locale
    )
  }
}