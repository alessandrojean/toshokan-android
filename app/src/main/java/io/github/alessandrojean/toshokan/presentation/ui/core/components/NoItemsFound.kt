package io.github.alessandrojean.toshokan.presentation.ui.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun NoItemsFound(
  modifier: Modifier = Modifier,
  text: String,
  icon: ImageVector? = null
) {
  NoItemsFound(
    modifier = modifier,
    text = {
      Text(
        text = text,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyMedium.copy(
          color = LocalContentColor.current.copy(alpha = 0.8f)
        )
      )
    },
    icon = icon
  )
}

@Composable
fun NoItemsFound(
  modifier: Modifier = Modifier,
  text: @Composable (() -> Unit)? = null,
  icon: ImageVector? = null
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .then(modifier),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    if (icon != null) {
      Icon(
        painter = rememberVectorPainter(icon),
        contentDescription = null,
        tint = LocalContentColor.current.copy(alpha = 0.15f),
        modifier = Modifier
          .size(96.dp)
          .padding(bottom = if (text != null) 16.dp else 0.dp)
      )
    }

    text?.invoke()
  }
}