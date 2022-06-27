package io.github.alessandrojean.toshokan.presentation.ui.core.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.alessandrojean.toshokan.database.data.Book
import io.github.alessandrojean.toshokan.presentation.extensions.surfaceWithTonalElevation
import io.github.alessandrojean.toshokan.presentation.extensions.withTonalElevation

@Composable
fun BookCard(
  modifier: Modifier = Modifier,
  book: Book?,
  selected: Boolean = false,
  shape: Shape = MaterialTheme.shapes.medium,
  onClick: () -> Unit,
  onLongClick: () -> Unit = {}
) {
  val context = LocalContext.current

  val idKey = book?.id?.toString() ?: "null"
  var imageLoaded by rememberSaveable(key = "book_loaded_$idKey") { mutableStateOf(false) }
  var aspectRatio by rememberSaveable(key = "book_aspect_$idKey") { mutableStateOf(2f / 3f) }

  val selectedColor = MaterialTheme.colorScheme.surfaceTint

  val borderWidth by animateDpAsState(if (selected) 2.dp else 0.dp)
  val borderColor by animateColorAsState(
    if (selected) selectedColor else Color.Transparent
  )

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(shape)
      .combinedClickable(
        enabled = book != null,
        onClick = onClick,
        onLongClick = onLongClick
      )
      .then(modifier),
    shape = shape,
    color = if (selected) {
      MaterialTheme.colorScheme.surfaceVariant.withTonalElevation(4.dp)
    } else {
      MaterialTheme.colorScheme.surface
    }
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
          .clip(shape)
          .drawWithContent {
            drawContent()
            if (selected) {
              drawRect(
                color = selectedColor.copy(alpha = 0.35f),
                topLeft = Offset.Zero,
                size = size
              )
            }
          }
          .border(BorderStroke(borderWidth, borderColor), shape),
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