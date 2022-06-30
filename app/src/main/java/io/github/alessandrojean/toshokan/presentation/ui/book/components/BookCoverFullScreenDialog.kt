package io.github.alessandrojean.toshokan.presentation.ui.book.components

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmapOrNull
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.Book
import io.github.alessandrojean.toshokan.presentation.ui.core.components.ZoomableImage
import io.github.alessandrojean.toshokan.util.extension.bottom
import io.github.alessandrojean.toshokan.util.extension.top
import io.github.alessandrojean.toshokan.util.extension.topPadding

@Composable
fun BookCoverFullScreenDialog(
  modifier: Modifier = Modifier,
  containerColor: Color = MaterialTheme.colorScheme.background,
  book: Book?,
  onShareClick: (Bitmap?) -> Unit,
  onSaveClick: (Bitmap?) -> Unit,
  onEditClick: () -> Unit,
  onDeleteClick: () -> Unit,
  onDismiss: () -> Unit
) {
  val imagePainter = rememberAsyncImagePainter(
    model = ImageRequest.Builder(LocalContext.current)
      .data(book)
      .size(Size.ORIGINAL)
      .build()
  )
  val imageState = imagePainter.state

  Scaffold(
    containerColor = containerColor,
    modifier = modifier,
    topBar = {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(WindowInsets.statusBars.topPadding)
          .background(containerColor.copy(alpha = 0.9f))
      )
    },
    bottomBar = {
      BottomAppBar(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = containerColor.copy(alpha = 0.9f),
        tonalElevation = 0.dp
      ) {
        IconButton(onClick = onDismiss) {
          Icon(
            imageVector = Icons.Outlined.Close,
            contentDescription = stringResource(R.string.action_cancel)
          )
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
          enabled = imageState is AsyncImagePainter.State.Success,
          onClick = {
            if (imageState is AsyncImagePainter.State.Success) {
              onShareClick(imageState.result.drawable.toBitmapOrNull())
            }
          }
        ) {
          Icon(
            imageVector = Icons.Outlined.Share,
            contentDescription = stringResource(R.string.action_share)
          )
        }

        IconButton(
          enabled = imageState is AsyncImagePainter.State.Success,
          onClick = {
            if (imageState is AsyncImagePainter.State.Success) {
              onSaveClick(imageState.result.drawable.toBitmapOrNull())
            }
          }
        ) {
          Icon(
            imageVector = Icons.Outlined.FileDownload,
            contentDescription = stringResource(R.string.action_save_image)
          )
        }

        Box {
          var editExpanded by remember { mutableStateOf(false) }

          IconButton(
            enabled = imageState is AsyncImagePainter.State.Success,
            onClick = { editExpanded = true }
          ) {
            Icon(
              imageVector = Icons.Outlined.MoreVert,
              contentDescription = stringResource(R.string.action_edit)
            )
          }

          DropdownMenu(
            expanded = editExpanded,
            onDismissRequest = { editExpanded = false }
          ) {
            DropdownMenuItem(
              text = { Text(stringResource(R.string.action_edit)) },
              onClick = {
                onEditClick()
                editExpanded = false
              }
            )
            DropdownMenuItem(
              text = { Text(stringResource(R.string.action_delete)) },
              onClick = {
                onDeleteClick()
                editExpanded = false
              }
            )
          }
        }

      }
    },
    content = { innerPadding ->
      val topPadding = innerPadding.top
      val bottomPadding = innerPadding.bottom

      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(containerColor)
      ) {
        ZoomableImage(
          modifier = Modifier
            .fillMaxSize()
            .align(Alignment.Center),
          state = imageState,
          contentDescription = book?.title,
          contentPadding = PaddingValues(
            top = topPadding,
            bottom = bottomPadding
          )
        )
      }
    }
  )
}
