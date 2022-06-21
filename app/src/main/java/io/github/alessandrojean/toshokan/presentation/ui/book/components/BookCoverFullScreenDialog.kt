package io.github.alessandrojean.toshokan.presentation.ui.book.components

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.graphics.drawable.toBitmapOrNull
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.Book
import io.github.alessandrojean.toshokan.presentation.ui.core.components.ZoomableImage
import io.github.alessandrojean.toshokan.util.extension.bottomPadding

@Composable
fun BookCoverFullScreenDialog(
  modifier: Modifier = Modifier,
  containerColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
  book: Book?,
  onShareClick: (Bitmap?) -> Unit,
  onSaveClick: (Bitmap?) -> Unit,
  onEditClick: () -> Unit,
  onDismiss: () -> Unit
) {
  val topAppBarBackgroundColors = TopAppBarDefaults.smallTopAppBarColors()
  val topAppBarBackground = topAppBarBackgroundColors.containerColor(scrollFraction = 0f).value

  val imagePainter = rememberAsyncImagePainter(
    model = ImageRequest.Builder(LocalContext.current)
      .data(book)
      .size(Size.ORIGINAL)
      .build()
  )
  val imageState = imagePainter.state

//  val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues()
//  val navigationBarHeightPx = with(LocalDensity.current) { navigationBarHeight.toPx() }

  Scaffold(
    containerColor = containerColor,
    modifier = modifier,
    topBar = {
      Surface(color = topAppBarBackground) {
        SmallTopAppBar(
          modifier = Modifier.statusBarsPadding(),
          colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = Color.Transparent
          ),
          navigationIcon = {
            IconButton(onClick = onDismiss) {
              Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = stringResource(R.string.action_cancel)
              )
            }
          },
          title = {},
          actions = {
            IconButton(
              enabled = imageState is AsyncImagePainter.State.Success,
              onClick = onEditClick
            ) {
              Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = stringResource(R.string.action_edit)
              )
            }

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
          }
        )
      }
    },
    content = { innerPadding ->
      val navigationBarsPadding = WindowInsets.navigationBars.bottomPadding

      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        ZoomableImage(
          modifier = Modifier
            .fillMaxSize()
            .align(Alignment.Center),
          state = imageState,
          contentDescription = book?.title,
          contentPadding = PaddingValues(
            top = navigationBarsPadding / 2f,
            bottom = navigationBarsPadding
          ),
          extraSpace = PaddingValues(
            bottom = navigationBarsPadding
          )
        )
      }
    }
  )
}
