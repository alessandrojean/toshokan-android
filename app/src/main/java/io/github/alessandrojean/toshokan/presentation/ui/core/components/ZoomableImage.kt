package io.github.alessandrojean.toshokan.presentation.ui.core.components

/**
 * Took originally from ComposeZoomableImage.
 * https://github.com/umutsoysl/ComposeZoomableImage
 *
 * Copyright 2021 Umut Soysal.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.updatePadding
import coil.compose.AsyncImagePainter
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import io.github.alessandrojean.toshokan.util.extension.bottom
import io.github.alessandrojean.toshokan.util.extension.end
import io.github.alessandrojean.toshokan.util.extension.start
import io.github.alessandrojean.toshokan.util.extension.top

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
  val density = LocalDensity.current
  val (topPadding, bottomPadding) = with(density) {
    contentPadding.top.roundToPx() to contentPadding.bottom.roundToPx()
  }
  val (startExtra, topExtra, endExtra, bottomExtra) = with(density) {
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
        setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_INSIDE)
        setExtraSpace(startExtra, topExtra, endExtra, bottomExtra)
        setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE)
        updatePadding(bottom = bottomPadding, top = topPadding)
      }
    },
    update = { view ->
      if (state is AsyncImagePainter.State.Success) {
        view.setImage(ImageSource.bitmap(state.result.drawable.toBitmap()))
      }
    }
  )
}
