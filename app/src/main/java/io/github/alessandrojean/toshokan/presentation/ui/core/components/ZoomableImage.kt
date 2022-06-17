package io.github.alessandrojean.toshokan.presentation.ui.core.components

/**
 * Token from ComposeZoomableImage.
 * https://github.com/umutsoysl/ComposeZoomableImage
 *
 * Copyright 2021 Umut Soysal.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.awaitCancellation

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

    offset = Offset(
      x = if (currentWidth >= size.width) {
        val sizeDiff = (currentWidth - size.width) / 2
        offset.x.coerceIn(minimumValue = -sizeDiff, maximumValue = sizeDiff)
      } else {
        val sizeDiff = (size.width - currentWidth) / 2
        offset.x.coerceIn(minimumValue = -sizeDiff, maximumValue = sizeDiff)
      },
      y = if (currentHeight >= size.height) {
        val sizeDiff = (currentHeight - size.height) / 2
        offset.y.coerceIn(minimumValue = -sizeDiff, maximumValue = sizeDiff)
      } else {
        val sizeDiff = (size.height - currentHeight) / 2
        offset.y.coerceIn(minimumValue = -sizeDiff, maximumValue = sizeDiff)
      }
    )
  }

  Image(
    painter = painter,
    contentDescription = contentDescription,
    contentScale = contentScale,
    modifier = Modifier
      .fillMaxSize()
      .onGloballyPositioned { size = it.size }
      .transformable(
        enabled = isZoomable,
        state = rememberTransformableState { zoomChange, panChange, _ ->
          scale = (scale * zoomChange).coerceIn(minimumValue = minScale, maximumValue = maxScale)

          if (scale > minScale) {
            offset += panChange
          }

          limitOffset()
        },
        lockRotationOnZoomPan = true
      )
      .pointerInput(Unit) {
        detectTapGestures(
          onDoubleTap = {
            offset = initialOffset
            scale = if (scale == minScale) maxScale else minScale
          }
        )
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
