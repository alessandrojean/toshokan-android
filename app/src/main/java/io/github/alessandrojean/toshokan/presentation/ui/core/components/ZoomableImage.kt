package io.github.alessandrojean.toshokan.presentation.ui.core.components

/**
 * Took originally from ComposeZoomableImage.
 * https://github.com/umutsoysl/ComposeZoomableImage
 *
 * Copyright 2021 Umut Soysal.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import io.github.alessandrojean.toshokan.util.extension.topPadding
import logcat.logcat
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun ZoomableImage(
  painter: Painter,
  modifier: Modifier = Modifier,
  contentDescription: String? = null,
  initialOffset: Offset = Offset.Zero,
  minScale: Float = 1f,
  maxScale: Float = 3f,
  contentScale: ContentScale = ContentScale.Fit,
  isZoomable: Boolean = true,
) {
  var size by remember { mutableStateOf(IntSize.Zero) }
  var scale by remember { mutableStateOf(1f) }
  var offset by remember { mutableStateOf(initialOffset) }

  val scaleAnimated by animateFloatAsState(scale)
  val offsetAnimated by animateOffsetAsState(offset)

  fun limitOffset() {
    if (painter.intrinsicSize.isUnspecified) {
      return
    }

    val srcSize = Size(painter.intrinsicSize.width, painter.intrinsicSize.height)
    val destSize = size.toSize()
    val scaleFactor: ScaleFactor = contentScale.computeScaleFactor(srcSize, destSize)

    val currentWidth = painter.intrinsicSize.width * scale * scaleFactor.scaleX
    val currentHeight = painter.intrinsicSize.height * scale * scaleFactor.scaleY

    val maxWidth = max(currentWidth, size.width.toFloat())
    val minWidth = min(currentWidth, size.width.toFloat())

    val maxHeight = max(currentHeight, size.height.toFloat())
    val minHeight = min(currentHeight, size.height.toFloat())
    offset = Offset(
      x = offset.x.coerceIn(
        minimumValue = -(maxWidth - minWidth) / 2,
        maximumValue = (maxWidth - minWidth) / 2
      ),
      y = offset.y.coerceIn(
        minimumValue = -(maxHeight - minHeight) / 2,
        maximumValue = (maxHeight - minHeight) / 2
      )
    )
  }

  Image(
    painter = painter,
    contentDescription = contentDescription,
    contentScale = contentScale,
    modifier = Modifier
      .fillMaxSize()
      .onGloballyPositioned { size = it.size }
      .pointerInput(isZoomable) {
        if (isZoomable) {
          detectTapGestures(
            onDoubleTap = {
              offset = initialOffset
              scale = if (scale == minScale) maxScale else minScale
            }
          )
        }
      }
      .pointerInput(isZoomable) {
        if (isZoomable) {
          detectTransformGestures(
            panZoomLock = true,
            onGesture = { _, panChange, zoomChange, _ ->
              scale = (scale * zoomChange).coerceIn(minimumValue = minScale, maximumValue = maxScale)

              if (scale > minScale) {
                offset += panChange
              }

              limitOffset()
            }
          )
        }
      }
      .graphicsLayer {
        if (isZoomable) {
          scaleX = scaleAnimated
          scaleY = scaleAnimated
          translationX = offsetAnimated.x
          translationY = offsetAnimated.y
        }
      }
      .then(modifier)
  )
}
