package io.github.alessandrojean.toshokan.presentation.ui.core.components

/**
 * Took originally from ComposeZoomableImage.
 * https://github.com/umutsoysl/ComposeZoomableImage
 *
 * Copyright 2021 Umut Soysal.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

import android.graphics.Bitmap
import android.graphics.PointF
import android.view.ViewGroup
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.core.graphics.plus
import coil.compose.AsyncImagePainter
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import io.github.alessandrojean.toshokan.util.extension.bottom
import io.github.alessandrojean.toshokan.util.extension.end
import io.github.alessandrojean.toshokan.util.extension.start
import io.github.alessandrojean.toshokan.util.extension.top
import io.github.alessandrojean.toshokan.util.extension.topPadding
import logcat.logcat
import java.lang.Exception
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun ZoomableImage(
  state: AsyncImagePainter.State,
  modifier: Modifier = Modifier,
  contentDescription: String? = null,
  minScale: Float = 1f,
  maxScale: Float = 8f,
  contentPadding: PaddingValues = PaddingValues(all = 0.dp),
  extraSpace: PaddingValues = PaddingValues(all = 0.dp),
) {
  val (startPadding, topPadding, endPadding, bottomPadding) = with(LocalDensity.current) {
    arrayOf(
      contentPadding.start.roundToPx(),
      contentPadding.top.roundToPx(),
      contentPadding.end.roundToPx(),
      contentPadding.bottom.roundToPx()
    )
  }
  val (startExtra, topExtra, endExtra, bottomExtra) = with(LocalDensity.current) {
    arrayOf(
      extraSpace.start.toPx(),
      extraSpace.top.toPx(),
      extraSpace.end.toPx(),
      extraSpace.bottom.toPx()
    )
  }

  AndroidView(
    modifier = Modifier
      .fillMaxSize()
      .semantics {
        contentDescription?.let {
          this.contentDescription = it
        }
      }
      .then(modifier),
    factory = { context ->
      SubsamplingScaleImageView(context).apply {
        this.minScale = minScale
        this.maxScale = maxScale
        setDoubleTapZoomScale((maxScale - minScale) / 2f)
        setDoubleTapZoomStyle(SubsamplingScaleImageView.ZOOM_FOCUS_CENTER)
        setPadding(
          startPadding.coerceAtLeast(startPadding),
          topPadding.coerceAtLeast(startPadding),
          endPadding.coerceAtLeast(startPadding),
          bottomPadding.coerceAtLeast(startPadding)
        )
        setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_INSIDE)
        setExtraSpace(startExtra, topExtra, endExtra, bottomExtra)
        setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE)
      }
    },
    update = { view ->
      if (state is AsyncImagePainter.State.Success) {
        view.setImage(ImageSource.bitmap(state.result.drawable.toBitmap()))
      }
    }
  )
}

//@Composable
//fun ZoomableImage(
//  painter: Painter,
//  modifier: Modifier = Modifier,
//  contentDescription: String? = null,
//  minScale: Float = 1f,
//  maxScale: Float = 3f,
//  contentScale: ContentScale = ContentScale.Fit,
//  contentPadding:PaddingValues = PaddingValues(all = 0.dp)
//) {
//  val density = LocalDensity.current
//  val (paddingStart, paddingEnd) = with(density) {
//    contentPadding.start.toPx() to contentPadding.end.toPx()
//  }
//  val (paddingTop, paddingBottom) = with(density) {
//    contentPadding.top.toPx() to contentPadding.bottom.toPx()
//  }
//
//  val initialOffset = Offset(
//    x = (paddingStart + paddingEnd) / 2f,
//    y = (paddingTop + paddingBottom) / 2f
//  )
//
//  var size by remember { mutableStateOf(IntSize.Zero) }
//  var position by remember { mutableStateOf(Offset.Zero) }
//  var scale by remember { mutableStateOf(1f) }
//  var offset by remember { mutableStateOf(initialOffset) }
//
//  val scaleAnimated by animateFloatAsState(scale)
//  val offsetAnimated by animateOffsetAsState(-offset * scale)
//
//  fun limitOffset(oldScale: Float, centroid: Offset = initialOffset) {
//    if (painter.intrinsicSize.isUnspecified) {
//      return
//    }
//
//    val srcSize = Size(painter.intrinsicSize.width, painter.intrinsicSize.height)
//    val destSize = size.toSize()
//    val scaleFactor: ScaleFactor = contentScale.computeScaleFactor(srcSize, destSize)
//
//    val currentWidth = painter.intrinsicSize.width * scale * scaleFactor.scaleX
//    val currentHeight = painter.intrinsicSize.height * scale * scaleFactor.scaleY
//
//    val maxWidth = max(currentWidth, size.width.toFloat())
//    val minWidth = min(currentWidth, size.width.toFloat())
//
//    val maxHeight = max(currentHeight, size.height.toFloat())
//    val minHeight = min(currentHeight, size.height.toFloat())
//
//    offset = Offset(
//      x = if (currentWidth > size.width || scale > oldScale) {
//        offset.x.coerceIn(
//          minimumValue = position.x + paddingStart / scale,
//          maximumValue = position.x + (maxWidth - minWidth + paddingEnd) / scale
//        )
//      } else {
//        centroid.x
//      },
//      y = if (currentHeight > size.height || scale > oldScale) {
//        offset.y.coerceIn(
//          minimumValue = position.y + paddingTop / scale,
//          maximumValue = position.y + (maxHeight - minHeight + paddingBottom) / scale
//        )
//      } else {
//        centroid.y
//      }
//    )
//  }
//
//  fun handleOffsetChange(oldScale: Float, centroid: Offset, pan: Offset) {
//    if (painter.intrinsicSize.isUnspecified) {
//      return
//    }
//
//    val srcSize = Size(painter.intrinsicSize.width, painter.intrinsicSize.height)
//    val destSize = size.toSize()
//    val scaleFactor: ScaleFactor = contentScale.computeScaleFactor(srcSize, destSize)
//
//    val currentWidth = painter.intrinsicSize.width * scale * scaleFactor.scaleX
//    val currentHeight = painter.intrinsicSize.height * scale * scaleFactor.scaleY
//
//    offset = (offset + centroid / oldScale) - (centroid / scale + pan / oldScale)
//  }
//
//  Image(
//    painter = painter,
//    contentDescription = contentDescription,
//    contentScale = contentScale,
//    modifier = Modifier
//      .fillMaxSize()
//      .onGloballyPositioned { coordinates ->
//        size = coordinates.size
//        position = (coordinates.positionInWindow() + coordinates.localToWindow(-initialOffset)) / 2f
//      }
//      .pointerInput(Unit) {
//        detectTapGestures(
//          onDoubleTap = {
//            if (scale == minScale) {
//              scale = maxScale
//              handleOffsetChange(minScale, it, initialOffset)
//            } else {
//              scale = minScale
//              offset = initialOffset
//            }
//
////            limitOffset()
//          }
//        )
//      }
//      .pointerInput(Unit) {
//        detectTransformGestures(
//          panZoomLock = true,
//          onGesture = { centroid, panChange, zoomChange, _ ->
//            val oldScale = scale
//            scale = (scale * zoomChange).coerceIn(minScale, maxScale)
//
//            if (scale > minScale) {
//              handleOffsetChange(oldScale, centroid, panChange)
//            }
//
//            limitOffset(oldScale)
//          }
//        )
//      }
//      .graphicsLayer {
//        scaleX = scaleAnimated
//        scaleY = scaleAnimated
//        translationX = offsetAnimated.x
//        translationY = offsetAnimated.y
//        transformOrigin = TransformOrigin(0f, 0f)
//      }
//      .then(modifier)
//  )
//}