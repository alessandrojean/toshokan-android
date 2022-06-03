package io.github.alessandrojean.toshokan.presentation.ui.core.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import io.github.alessandrojean.toshokan.presentation.extensions.surfaceWithTonalElevation

@Composable
fun BookCard(
  modifier: Modifier = Modifier,
  title: String,
  coverUrl: String?,
  isFuture: Boolean,
  shape: Shape = MaterialTheme.shapes.large,
  onClick: () -> Unit
) {
  var imageLoaded by remember { mutableStateOf(false) }

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(shape)
      .clickable(onClick = onClick)
      .then(modifier),
    shape = shape
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(2f / 3f)
        .padding(4.dp),
      contentAlignment = Alignment.Center
    ) {
      AnimatedVisibility(
        visible = !imageLoaded,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceWithTonalElevation(6.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            modifier = Modifier.size(32.dp),
            imageVector = Icons.Outlined.Book,
            contentDescription = null,
            tint = LocalContentColor.current.copy(alpha = 0.5f)
          )
        }
      }
      AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
          .data(coverUrl)
          .crossfade(true)
          .build(),
        modifier = Modifier.clip(shape),
        contentDescription = title,
        contentScale = ContentScale.Inside,
        onSuccess = { imageLoaded = true }
      )
    }
  }
}