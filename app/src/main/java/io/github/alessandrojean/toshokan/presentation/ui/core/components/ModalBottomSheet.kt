package io.github.alessandrojean.toshokan.presentation.ui.core.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.presentation.ui.theme.DividerOpacity
import io.github.alessandrojean.toshokan.presentation.ui.theme.ModalBottomSheetLargeShape
import io.github.alessandrojean.toshokan.util.extension.bottom
import io.github.alessandrojean.toshokan.util.extension.end
import io.github.alessandrojean.toshokan.util.extension.start
import io.github.alessandrojean.toshokan.util.extension.top

@Composable
fun ModalBottomSheet(
  modifier: Modifier = Modifier,
  color: Color = MaterialTheme.colorScheme.surface,
  tonalElevation: Dp = 6.dp,
  contentPadding: PaddingValues = WindowInsets.navigationBars.asPaddingValues(),
  title: String,
  footer: @Composable (() -> Unit)? = null,
  content: @Composable () -> Unit
) {
  ModalBottomSheet(
    modifier = modifier,
    color = color,
    tonalElevation = tonalElevation,
    contentPadding = contentPadding,
    header = { ModalBottomSheetTitle(text = title) },
    footer = footer,
    content = content
  )
}

@Composable
fun ModalBottomSheet(
  modifier: Modifier = Modifier,
  color: Color = MaterialTheme.colorScheme.surface,
  tonalElevation: Dp = 6.dp,
  contentPadding: PaddingValues = WindowInsets.navigationBars.asPaddingValues(),
  header: @Composable (() -> Unit)? = null,
  footer: @Composable (() -> Unit)? = null,
  content: @Composable () -> Unit
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .then(modifier),
    tonalElevation = tonalElevation,
    color = color,
    shape = ModalBottomSheetLargeShape
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(contentPadding)
    ) {
      header?.let {
        header()
        Divider(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
          color = LocalContentColor.current.copy(alpha = DividerOpacity)
        )
      }
      content()
      footer?.let {
        Divider(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
          color = LocalContentColor.current.copy(alpha = DividerOpacity)
        )
        footer()
      }
    }
  }
}

@Composable
fun ModalBottomSheetTitle(
  text: String,
  contentPadding: PaddingValues = PaddingValues(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 8.dp)
) {
  Text(
    modifier = Modifier.padding(contentPadding),
    text = text,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    style = MaterialTheme.typography.titleLarge
  )
}

@Composable
fun ModalBottomSheetItem(
  modifier: Modifier = Modifier,
  text: String,
  icon: Painter,
  contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp),
  trailingContent: @Composable () -> Unit = {},
  onClick: () -> Unit
) {
  ModalBottomSheetItem(
    modifier = modifier,
    contentPadding = contentPadding,
    text = text,
    leadingContent = {
      Icon(
        painter = icon,
        contentDescription = text,
        tint = LocalContentColor.current
      )
    },
    trailingContent = trailingContent,
    onClick = onClick
  )
}

@Composable
fun ModalBottomSheetItem(
  modifier: Modifier = Modifier,
  text: String,
  contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp),
  leadingContent: @Composable (() -> Unit)? = null,
  trailingContent: @Composable (() -> Unit)? = null,
  onClick: () -> Unit
) {
  val padding = PaddingValues(
    start = (contentPadding.start - 16.dp).coerceAtLeast(0.dp),
    end = (contentPadding.end - 16.dp).coerceAtLeast(0.dp),
    top = (contentPadding.top - 16.dp).coerceAtLeast(0.dp),
    bottom = (contentPadding.bottom - 16.dp).coerceAtLeast(0.dp)
  )

  ListItem(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(padding)
      .then(modifier),
    leadingContent = leadingContent,
    headlineText = {
      Text(
        modifier = Modifier.padding(
          start = if (leadingContent != null) padding.start else 0.dp,
          end = if (trailingContent != null) padding.end else 0.dp
        ),
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    },
    trailingContent = trailingContent
  )
}