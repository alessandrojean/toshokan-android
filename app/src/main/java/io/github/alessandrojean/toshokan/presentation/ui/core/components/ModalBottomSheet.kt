package io.github.alessandrojean.toshokan.presentation.ui.core.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.presentation.ui.theme.DividerOpacity
import io.github.alessandrojean.toshokan.presentation.ui.theme.ModalBottomSheetLargeShape

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
  trailingIcon: @Composable () -> Unit = {},
  onClick: () -> Unit
) {
  ModalBottomSheetItem(
    modifier = modifier,
    text = text,
    leadingIcon = {
      Icon(
        painter = icon,
        contentDescription = text,
        tint = LocalContentColor.current
      )
    },
    trailingIcon = trailingIcon,
    onClick = onClick
  )
}

@Composable
fun ModalBottomSheetItem(
  modifier: Modifier = Modifier,
  text: String,
  leadingIcon: @Composable (() -> Unit)? = null,
  trailingIcon: @Composable (() -> Unit)? = null,
  contentPadding: PaddingValues = PaddingValues(vertical = 16.dp, horizontal = 24.dp),
  textPadding: PaddingValues = PaddingValues(
    start = if (leadingIcon != null) 24.dp else 0.dp,
    end = if (trailingIcon != null) 24.dp else 0.dp
  ),
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .clickable(onClick = onClick)
      .padding(contentPadding)
      .then(modifier),
    verticalAlignment = Alignment.CenterVertically
  ) {
    leadingIcon?.let { it() }
    Text(
      text = text,
      modifier = Modifier
        .padding(textPadding)
        .weight(1f),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
    trailingIcon?.let { it() }
  }
}