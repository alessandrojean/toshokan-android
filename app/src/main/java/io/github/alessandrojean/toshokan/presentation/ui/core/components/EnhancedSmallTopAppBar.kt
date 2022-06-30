package io.github.alessandrojean.toshokan.presentation.ui.core.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * A Enhanced SmallTopAppBar that supports content padding.
 * Useful where it's needed to provide the window insets.
 */
@Composable
fun EnhancedSmallTopAppBar(
  title: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  navigationIcon: @Composable () -> Unit = {},
  actions: @Composable RowScope.() -> Unit = {},
  colors: TopAppBarColors = TopAppBarDefaults.smallTopAppBarColors(),
  scrollBehavior: TopAppBarScrollBehavior? = null,
  contentPadding: PaddingValues = PaddingValues(),
  content: @Composable ColumnScope.() -> Unit = {}
) {
  EnhancedSmallTopAppBar(
    modifier = modifier,
    colors = colors,
    contentPadding = contentPadding,
    scrollBehavior = scrollBehavior,
    appBar = {
      SmallTopAppBar(
        title = title,
        navigationIcon = navigationIcon,
        actions = actions,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.smallTopAppBarColors(
          containerColor = Color.Transparent,
          scrolledContainerColor = Color.Transparent
        )
      )
    },
    content = content
  )
}

@Composable
fun EnhancedSmallTopAppBar(
  modifier: Modifier = Modifier,
  colors: TopAppBarColors = TopAppBarDefaults.smallTopAppBarColors(),
  contentPadding: PaddingValues = PaddingValues(),
  scrollBehavior: TopAppBarScrollBehavior? = null,
  appBar: @Composable () -> Unit,
  content: @Composable ColumnScope.() -> Unit = {}
) {
  val containerColor by colors.containerColor(scrollBehavior?.scrollFraction ?: 0f)

  Surface(
    modifier = modifier,
    color = containerColor
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(contentPadding)
    ) {
      appBar()
      content()
    }
  }
}