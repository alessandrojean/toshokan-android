package io.github.alessandrojean.toshokan.presentation.ui.library.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.ui.core.dialog.EnhancedAlertDialog
import io.github.alessandrojean.toshokan.util.extension.deviceHasCamera

@Composable
fun CreateBookDialog(
  onScanBarcodeClick: () -> Unit,
  onIsbnSearchClick: () -> Unit,
  onFillManuallyClick: () -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val hasCamera = remember { context.deviceHasCamera }

  EnhancedAlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = stringResource(R.string.create_book),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        if (hasCamera) {
          CreateBookDialogItem(
            icon = painterResource(R.drawable.ic_barcode_scanner_outlined),
            text = stringResource(R.string.action_scan_barcode),
            onClick = {
              onScanBarcodeClick()
              onDismiss()
            }
          )
        }
        CreateBookDialogItem(
          icon = rememberVectorPainter(Icons.Outlined.Search),
          text = stringResource(R.string.action_search_by_isbn),
          onClick = {
            onIsbnSearchClick()
            onDismiss()
          }
        )
        CreateBookDialogItem(
          icon = rememberVectorPainter(Icons.Outlined.EditNote),
          text = stringResource(R.string.action_fill_manually),
          onClick = {
            onFillManuallyClick()
            onDismiss()
          }
        )
      }
    },
  )
}

@Composable
fun CreateBookDialogItem(
  icon: Painter,
  text: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(vertical = 16.dp, horizontal = 24.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(24.dp)
  ) {
    Icon(
      painter = icon,
      contentDescription = text,
      tint = MaterialTheme.colorScheme.onSurface
    )
    Text(
      text = text,
      color = MaterialTheme.colorScheme.onSurface,
      style = MaterialTheme.typography.bodyLarge,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}