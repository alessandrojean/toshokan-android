package io.github.alessandrojean.toshokan.presentation.ui.core.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.alessandrojean.toshokan.util.extension.bottom
import io.github.alessandrojean.toshokan.util.extension.end
import io.github.alessandrojean.toshokan.util.extension.start
import io.github.alessandrojean.toshokan.util.extension.top
import kotlin.math.max

/**
 * A custom alert dialog that is padding-less on the content.
 */
@Composable
fun EnhancedAlertDialog(
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  confirmButton: @Composable (() -> Unit)? = null,
  dismissButton: @Composable (() -> Unit)? = null,
  icon: @Composable (() -> Unit)? = null,
  title: @Composable (() -> Unit)? = null,
  text: @Composable (() -> Unit)? = null,
  shape: Shape = MaterialTheme.shapes.extraLarge,
  containerColor: Color = MaterialTheme.colorScheme.surface,
  tonalElevation: Dp = 6.dp,
  iconContentColor: Color = MaterialTheme.colorScheme.secondary,
  titleContentColor: Color = MaterialTheme.colorScheme.onSurface,
  textContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
  properties: DialogProperties = DialogProperties()
) {
  Dialog(
    onDismissRequest = onDismissRequest,
    properties = properties
  ) {
    EnhancedAlertDialogContent(
      buttons = if (dismissButton != null || confirmButton != null) {
        {
          EnhancedAlertDialogFlowRow(
            mainAxisSpacing = ButtonsMainAxisSpacing,
            crossAxisSpacing = ButtonsCrossAxisSpacing
          ) {
            dismissButton?.invoke()
            confirmButton?.invoke()
          }
        }
      } else null,
      modifier = modifier,
      icon = icon,
      title = title,
      text = text,
      shape = shape,
      containerColor = containerColor,
      tonalElevation = tonalElevation,
      // Note that a button content color is provided here from the dialog's token, but in
      // most cases, TextButtons should be used for dismiss and confirm buttons.
      // TextButtons will not consume this provided content color value, and will used their
      // own defined or default colors.
      buttonContentColor = MaterialTheme.colorScheme.primary,
      iconContentColor = iconContentColor,
      titleContentColor = titleContentColor,
      textContentColor = textContentColor,
    )
  }
}

private val ButtonsMainAxisSpacing = 8.dp
private val ButtonsCrossAxisSpacing = 12.dp

@Composable
fun EnhancedAlertDialogContent(
  modifier: Modifier = Modifier,
  buttons: @Composable (() -> Unit)? = null,
  icon: (@Composable () -> Unit)?,
  title: (@Composable () -> Unit)?,
  text: @Composable (() -> Unit)?,
  shape: Shape,
  containerColor: Color,
  tonalElevation: Dp,
  buttonContentColor: Color,
  iconContentColor: Color,
  titleContentColor: Color,
  textContentColor: Color,
) {
  Surface(
    modifier = modifier,
    shape = shape,
    color = containerColor,
    tonalElevation = tonalElevation,
  ) {
    Column(
      modifier = Modifier
        .sizeIn(minWidth = MinWidth, maxWidth = MaxWidth)
        .padding(top = DialogPadding.top, bottom = DialogPadding.bottom)
    ) {
      icon?.let {
        CompositionLocalProvider(LocalContentColor provides iconContentColor) {
          Box(
            Modifier
              .padding(IconPadding)
              .align(Alignment.CenterHorizontally)
          ) {
            icon()
          }
        }
      }
      title?.let {
        CompositionLocalProvider(LocalContentColor provides titleContentColor) {
          val textStyle = MaterialTheme.typography.headlineSmall
          ProvideTextStyle(textStyle) {
            Box(
              // Align the title to the center when an icon is present.
              Modifier
                .padding(TitlePadding)
                .align(
                  if (icon == null) {
                    Alignment.Start
                  } else {
                    Alignment.CenterHorizontally
                  }
                )
            ) {
              title()
            }
          }
        }
      }
      text?.let {
        CompositionLocalProvider(LocalContentColor provides textContentColor) {
          val textStyle = MaterialTheme.typography.bodyMedium
          ProvideTextStyle(textStyle) {
            Box(
              Modifier
                .weight(weight = 1f, fill = false)
                .padding(if (buttons != null) TextPadding else PaddingValues(bottom = 8.dp))
                .align(Alignment.Start)
            ) {
              text()
            }
          }
        }
      }
      if (buttons != null) {
        Box(
          modifier = Modifier
            .align(Alignment.End)
            .padding(start = DialogPadding.start, end = DialogPadding.end)
        ) {
          CompositionLocalProvider(LocalContentColor provides buttonContentColor) {
            val textStyle = MaterialTheme.typography.labelLarge
            ProvideTextStyle(value = textStyle, content = buttons)
          }
        }
      }
    }
  }
}

@Composable
fun EnhancedAlertDialogFlowRow(
  mainAxisSpacing: Dp,
  crossAxisSpacing: Dp,
  content: @Composable () -> Unit
) {
  Layout(content) { measurables, constraints ->
    val sequences = mutableListOf<List<Placeable>>()
    val crossAxisSizes = mutableListOf<Int>()
    val crossAxisPositions = mutableListOf<Int>()

    var mainAxisSpace = 0
    var crossAxisSpace = 0

    val currentSequence = mutableListOf<Placeable>()
    var currentMainAxisSize = 0
    var currentCrossAxisSize = 0

    // Return whether the placeable can be added to the current sequence.
    fun canAddToCurrentSequence(placeable: Placeable) =
      currentSequence.isEmpty() || currentMainAxisSize + mainAxisSpacing.roundToPx() +
        placeable.width <= constraints.maxWidth

    // Store current sequence information and start a new sequence.
    fun startNewSequence() {
      if (sequences.isNotEmpty()) {
        crossAxisSpace += crossAxisSpacing.roundToPx()
      }
      sequences += currentSequence.toList()
      crossAxisSizes += currentCrossAxisSize
      crossAxisPositions += crossAxisSpace

      crossAxisSpace += currentCrossAxisSize
      mainAxisSpace = max(mainAxisSpace, currentMainAxisSize)

      currentSequence.clear()
      currentMainAxisSize = 0
      currentCrossAxisSize = 0
    }

    for (measurable in measurables) {
      // Ask the child for its preferred size.
      val placeable = measurable.measure(constraints)

      // Start a new sequence if there is not enough space.
      if (!canAddToCurrentSequence(placeable)) startNewSequence()

      // Add the child to the current sequence.
      if (currentSequence.isNotEmpty()) {
        currentMainAxisSize += mainAxisSpacing.roundToPx()
      }
      currentSequence.add(placeable)
      currentMainAxisSize += placeable.width
      currentCrossAxisSize = max(currentCrossAxisSize, placeable.height)
    }

    if (currentSequence.isNotEmpty()) startNewSequence()

    val mainAxisLayoutSize = max(mainAxisSpace, constraints.minWidth)

    val crossAxisLayoutSize = max(crossAxisSpace, constraints.minHeight)

    val layoutWidth = mainAxisLayoutSize

    val layoutHeight = crossAxisLayoutSize

    layout(layoutWidth, layoutHeight) {
      sequences.forEachIndexed { i, placeables ->
        val childrenMainAxisSizes = IntArray(placeables.size) { j ->
          placeables[j].width +
            if (j < placeables.lastIndex) mainAxisSpacing.roundToPx() else 0
        }
        val arrangement = Arrangement.Bottom
        // TODO(soboleva): rtl support
        // Handle vertical direction
        val mainAxisPositions = IntArray(childrenMainAxisSizes.size) { 0 }
        with(arrangement) {
          arrange(mainAxisLayoutSize, childrenMainAxisSizes, mainAxisPositions)
        }
        placeables.forEachIndexed { j, placeable ->
          placeable.place(
            x = mainAxisPositions[j],
            y = crossAxisPositions[i]
          )
        }
      }
    }
  }
}

// Paddings for each of the dialog's parts.
private val DialogPadding = PaddingValues(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 18.dp)
val DialogHorizontalPadding = PaddingValues(start = 24.dp, end = 24.dp)
private val IconPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 16.dp)
private val TitlePadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 16.dp)
private val TextPadding = PaddingValues(bottom = 18.dp)

private val MinWidth = 280.dp
private val MaxWidth = 560.dp