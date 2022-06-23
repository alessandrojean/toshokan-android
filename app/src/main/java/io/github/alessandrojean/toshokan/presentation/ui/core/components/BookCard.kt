package io.github.alessandrojean.toshokan.presentation.ui.core.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import coil.request.ImageRequest
import io.github.alessandrojean.toshokan.database.data.Book
import io.github.alessandrojean.toshokan.presentation.extensions.surfaceWithTonalElevation

@Composable
fun BookCard(
  modifier: Modifier = Modifier,
  book: Book?,
  shape: Shape = MaterialTheme.shapes.medium,
  onClick: () -> Unit
) {
  val context = LocalContext.current

  var imageLoaded by remember { mutableStateOf(false) }
  var aspectRatio by remember { mutableStateOf(2f / 3f) }

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(shape)
      .clickable(
        enabled = book != null,
        onClick = onClick
      )
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
            .clip(shape)
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
        model = remember(book) {
          ImageRequest.Builder(context)
            .data(book)
            .crossfade(true)
            .size(400)
            .build()
        },
        modifier = Modifier
          .aspectRatio(aspectRatio)
          .fillMaxWidth()
          .clip(shape),
        contentDescription = book?.title,
        contentScale = ContentScale.Fit,
        onSuccess = { state ->
          imageLoaded = true
          aspectRatio = state.painter.intrinsicSize.width / state.painter.intrinsicSize.height
        }
      )
    }
  }
}