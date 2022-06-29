package io.github.alessandrojean.toshokan.presentation.ui.book.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.extensions.withTonalElevation

@Composable
fun BookSynopsis(
  modifier: Modifier = Modifier,
  synopsis: String?,
  containerColor: Color = MaterialTheme.colorScheme.surface,
  tonalElevation: Dp = 6.dp,
  onSynopsisToggleableChange: (Boolean) -> Unit = {},
  onSynopsisExpandedChange: (Boolean) -> Unit = {}
) {
  var synopsisExpanded by remember { mutableStateOf(false) }
  var synopsisToggleable by remember { mutableStateOf(false) }
  var synopsisLayoutResultState by remember { mutableStateOf<TextLayoutResult?>(null) }
  val synopsisBackground = containerColor.withTonalElevation(tonalElevation)

  val toggleButtonOffset by animateFloatAsState(if (synopsisExpanded) 0f else -18f)
  val toggleIconRotation by animateFloatAsState(if (synopsisExpanded) 180f else 0f)

  LaunchedEffect(synopsisLayoutResultState) {
    if (synopsisLayoutResultState == null) {
      return@LaunchedEffect
    }

    if (!synopsisExpanded && synopsisLayoutResultState!!.hasVisualOverflow) {
      synopsisToggleable = true
      onSynopsisToggleableChange(synopsisToggleable)
    }
  }

  if (synopsis.orEmpty().isNotBlank()) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .toggleable(
          value = synopsisExpanded,
          onValueChange = {
            synopsisExpanded = it
            onSynopsisExpandedChange(it)
          },
          enabled = synopsisToggleable,
          role = Role.Checkbox,
          indication = null,
          interactionSource = MutableInteractionSource()
        )
        .padding(
          bottom = if (synopsisToggleable) 4.dp else 12.dp,
          start = 24.dp,
          end = 24.dp
        )
        .animateContentSize()
        .then(modifier)
    ) {
      Text(
        text = synopsis.orEmpty().ifEmpty { stringResource(R.string.no_synopsis) },
        maxLines = if (synopsisExpanded) Int.MAX_VALUE else 4,
        onTextLayout = { synopsisLayoutResultState = it },
        overflow = TextOverflow.Clip,
        style = MaterialTheme.typography.bodyMedium.copy(
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontStyle = if (synopsis.orEmpty().isEmpty()) FontStyle.Italic else FontStyle.Normal
        )
      )
      if (synopsisToggleable) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .offset(y = toggleButtonOffset.dp)
            .background(
              Brush.verticalGradient(
                0.0f to Color.Transparent,
                0.2f to synopsisBackground.copy(alpha = 0.5f),
                0.8f to synopsisBackground
              )
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            modifier = Modifier.graphicsLayer(rotationX = toggleIconRotation),
            imageVector = Icons.Outlined.ExpandMore,
            contentDescription = null
          )
        }
      }
    }
  }
}