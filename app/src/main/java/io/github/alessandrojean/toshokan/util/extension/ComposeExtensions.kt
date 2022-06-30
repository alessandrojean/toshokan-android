package io.github.alessandrojean.toshokan.util.extension

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.fade
import com.google.accompanist.placeholder.material.placeholder
import io.github.alessandrojean.toshokan.presentation.extensions.selection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun PaddingValues.copy(
  top: Dp = this.calculateTopPadding(),
  bottom: Dp = this.calculateBottomPadding(),
  start: Dp = this.calculateStartPadding(LocalLayoutDirection.current),
  end: Dp = this.calculateEndPadding(LocalLayoutDirection.current)
): PaddingValues {
  return PaddingValues(
    top = top,
    bottom = bottom,
    start = start,
    end = end
  )
}

val PaddingValues.top: Dp
  @Composable get() = calculateTopPadding()

val PaddingValues.bottom: Dp
  @Composable get() = calculateBottomPadding()

val PaddingValues.start: Dp
  @Composable get() = calculateStartPadding(LocalLayoutDirection.current)

val PaddingValues.end: Dp
  @Composable get() = calculateEndPadding(LocalLayoutDirection.current)

val WindowInsets.Companion.navigationBarsWithIme: WindowInsets
  @Composable get() = navigationBars.union(WindowInsets.ime)

val WindowInsets.topPadding
  @Composable get() = asPaddingValues().top

val WindowInsets.bottomPadding
  @Composable get() = asPaddingValues().bottom

fun Modifier.navigationBarsWithImePadding() =
  composed { windowInsetsPadding(WindowInsets.navigationBarsWithIme) }

// TODO: Think of a better fix when the IME issue gets fixed.
// https://issuetracker.google.com/issues/192043120
fun Modifier.bringIntoViewOnFocus(scope: CoroutineScope): Modifier = composed {
  val bringIntoViewRequester = remember { BringIntoViewRequester() }

  bringIntoViewRequester(bringIntoViewRequester)
    .onFocusChanged {
      if (it.isFocused || it.hasFocus) {
        scope.launch {
//          delay(1000)
          bringIntoViewRequester.bringIntoView()
        }
      }
    }
}

@Composable
operator fun PaddingValues.plus(increment: PaddingValues): PaddingValues =
  PaddingValues(
    top = this.top + increment.top,
    bottom = this.bottom + increment.bottom,
    start = this.start + increment.start,
    end = this.end + increment.end
  )

val LazyListState.isLastItemVisible: Boolean
  get () = layoutInfo.visibleItemsInfo.lastOrNull()?.index == layoutInfo.totalItemsCount - 1

val LazyListState.isFirstItemVisible: Boolean
  get () = firstVisibleItemIndex == 0

val LazyListState.isAllItemsVisible: Boolean
  get () = layoutInfo.visibleItemsInfo.size == layoutInfo.totalItemsCount

data class LazyListScrollContext(
  val isTop: Boolean,
  val isBottom: Boolean,
  val isAllItemsVisible: Boolean
)

@Composable
fun rememberLazyListScrollContext(listState: LazyListState): LazyListScrollContext {
  val scrollContext by remember {
    derivedStateOf {
      LazyListScrollContext(
        isTop = listState.isFirstItemVisible,
        isBottom = listState.isLastItemVisible,
        isAllItemsVisible = listState.isAllItemsVisible
      )
    }
  }

  return scrollContext
}

fun Modifier.placeholder(visible: Boolean): Modifier = composed {
  Modifier.placeholder(
    visible = visible,
    color = MaterialTheme.colorScheme.selection,
    highlight = PlaceholderHighlight.fade()
  )
}