package io.github.alessandrojean.toshokan.presentation.ui.book.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmapOrNull
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import io.github.alessandrojean.toshokan.database.data.Book

@Composable
fun BookCoverBox(
  modifier: Modifier = Modifier,
  book: Book?,
  containerColor: Color = MaterialTheme.colorScheme.background,
  topBarHeightDp: Float = 64f,
  bottomOffsetDp: Float = 18f,
  onImageSuccess: (Bitmap?) -> Unit,
  onCoverClick: () -> Unit = {}
) {
  val verticalPadding = (topBarHeightDp + bottomOffsetDp).dp +
    WindowInsets.statusBars
      .asPaddingValues()
      .calculateTopPadding()
  var aspectRatio by remember { mutableStateOf(2f / 3f) }

  val coverPainter = rememberAsyncImagePainter(
    model = ImageRequest.Builder(LocalContext.current)
      .data(book)
      .crossfade(true)
      .allowHardware(false)
      .build(),
    contentScale = ContentScale.Fit,
    onSuccess = { state ->
      onImageSuccess(state.result.drawable.toBitmapOrNull())
      aspectRatio = state.painter.intrinsicSize.width / state.painter.intrinsicSize.height
    },
  )

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .aspectRatio(1f / 1.15f)
      .background(containerColor)
      .then(modifier),
    contentAlignment = Alignment.Center
  ) {
    AsyncImage(
      model = ImageRequest.Builder(LocalContext.current)
        .data(book)
        .crossfade(true)
        .build(),
      modifier = Modifier
        .fillMaxSize()
        .blur(4.dp)
        .graphicsLayer(alpha = 0.15f),
      contentDescription = book?.title,
      contentScale = ContentScale.Crop,
    )
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(
          top = verticalPadding,
          bottom = (topBarHeightDp + bottomOffsetDp).dp,
          start = 32.dp,
          end = 32.dp
        ),
      contentAlignment = Alignment.Center
    ) {
      if (coverPainter.state is AsyncImagePainter.State.Error || book?.cover_url.isNullOrBlank()) {
        Icon(
          modifier = Modifier.size(96.dp),
          imageVector = Icons.Outlined.Image,
          contentDescription = null,
          tint = LocalContentColor.current.copy(alpha = 0.15f)
        )
      }

      Image(
        modifier = Modifier
          .clickable(
            enabled = coverPainter.state is AsyncImagePainter.State.Success,
            onClick = onCoverClick,
            interactionSource = remember { MutableInteractionSource() },
            indication = null
          )
          .aspectRatio(aspectRatio)
          .fillMaxSize()
          .clip(MaterialTheme.shapes.large),
        painter = coverPainter,
        contentDescription = book?.title,
        contentScale = ContentScale.Fit,
      )
    }
  }
}
