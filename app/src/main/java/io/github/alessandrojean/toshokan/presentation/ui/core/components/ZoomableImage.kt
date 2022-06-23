package io.github.alessandrojean.toshokan.presentation.ui.core.components

/**
 * Took originally from ComposeZoomableImage.
 * https://github.com/umutsoysl/ComposeZoomableImage
 *
 * Copyright 2021 Umut Soysal.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
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
) {
  val density = LocalDensity.current
  val (topPadding, bottomPadding) = with(density) {
    contentPadding.top.roundToPx() to contentPadding.bottom.roundToPx()
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
      ZoomableImageView(context).apply {
        clipToPadding = false
        clipChildren = false
      }
    },
    update = { view ->
      if (state is AsyncImagePainter.State.Success) {
        view.setImage(state.result.drawable.toBitmap()) {
          this.minScale = minScale
          this.maxScale = maxScale
          setDoubleTapZoomScale((maxScale - minScale) / 2f)
        }

        view.updatePadding(bottom = bottomPadding, top = topPadding)
      }
    }
  )
}

private class ZoomableImageView constructor(context: Context) : FrameLayout(context) {

  private var imageView: SubsamplingScaleImageView? = null

  fun setImage(bitmap: Bitmap, block: SubsamplingScaleImageView.() -> Unit = {}) {
    if (imageView != null) {
      removeView(imageView)
    }

    imageView = SubsamplingScaleImageView(context).apply {
      setDoubleTapZoomStyle(SubsamplingScaleImageView.ZOOM_FOCUS_CENTER)
      setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_INSIDE)
      setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE)

      block()
    }

    addView(imageView, MATCH_PARENT, MATCH_PARENT)

    imageView?.setImage(ImageSource.bitmap(bitmap))
  }

}