package io.github.alessandrojean.toshokan.presentation.ui.book.components

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.core.graphics.drawable.toBitmapOrNull
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.Book
import io.github.alessandrojean.toshokan.presentation.ui.core.components.ZoomableImage

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

  val imagePainter = rememberAsyncImagePainter(model = book)

  val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
  val navigationBarHeightPx = with(LocalDensity.current) { navigationBarHeight.toPx() }

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
              enabled = imagePainter.state is AsyncImagePainter.State.Success,
              onClick = onEditClick
            ) {
              Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = stringResource(R.string.action_edit)
              )
            }

            IconButton(
              enabled = imagePainter.state is AsyncImagePainter.State.Success,
              onClick = {
                val imagePainterState = imagePainter.state

                if (imagePainterState is AsyncImagePainter.State.Success) {
                  onShareClick(imagePainterState.result.drawable.toBitmapOrNull())
                }
              }
            ) {
              Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = stringResource(R.string.action_share)
              )
            }

            IconButton(
              enabled = imagePainter.state is AsyncImagePainter.State.Success,
              onClick = {
                val imagePainterState = imagePainter.state

                if (imagePainterState is AsyncImagePainter.State.Success) {
                  onSaveClick(imagePainterState.result.drawable.toBitmapOrNull())
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
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(top = innerPadding.calculateTopPadding())
      ) {
        ZoomableImage(
          modifier = Modifier
            .fillMaxSize()
            .align(Alignment.Center),
          painter = imagePainter,
          contentDescription = book?.title,
          initialOffset = Offset(x = 0.0f, y = -navigationBarHeightPx / 2)
        )
      }
    }
  )
}
