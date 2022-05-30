package io.github.alessandrojean.toshokan.presentation.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.ui.barcodescanner.BarcodeScannerScreen
import io.github.alessandrojean.toshokan.presentation.ui.book.manage.ManageBookScreen
import io.github.alessandrojean.toshokan.presentation.ui.isbnlookup.IsbnLookupScreen

class LibraryScreen : AndroidScreen() {

  @Composable
  override fun Content() {
    var showCreateBookSheet by remember { mutableStateOf(false) }
    val navigator = LocalNavigator.currentOrThrow

    if (showCreateBookSheet) {
      CreateBookSheet(
        onScanBarcodeClick = { navigator.push(BarcodeScannerScreen()) },
        onIsbnSearchClick = { navigator.push(IsbnLookupScreen()) },
        onFillManuallyClick = { navigator.push(ManageBookScreen()) },
        onDismiss = { showCreateBookSheet = false }
      )
    }

    Scaffold(
      topBar = {
        SmallTopAppBar(
          modifier = Modifier.statusBarsPadding(),
          title = { Text(stringResource(R.string.library)) },
          actions = {
            IconButton(onClick = { /*TODO*/ }) {
              Icon(
                Icons.Default.Search,
                contentDescription = stringResource(R.string.action_search)
              )
            }
          }
        )
      },
      content = { innerPadding ->
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
          contentAlignment = Alignment.Center
        ) {
          Text(stringResource(R.string.library), modifier = Modifier.padding(innerPadding))
        }
      },
      floatingActionButtonPosition = FabPosition.End,
      floatingActionButton = {
        FloatingActionButton(
          onClick = { showCreateBookSheet = true }
        ) {
          Icon(
            Icons.Default.Add,
            contentDescription = stringResource(R.string.action_new_book)
          )
        }
      }
    )
  }

  @Composable
  fun CreateBookSheet(
    onScanBarcodeClick: () -> Unit,
    onIsbnSearchClick: () -> Unit,
    onFillManuallyClick: () -> Unit,
    onDismiss: () -> Unit
  ) {
    Dialog(onDismissRequest = onDismiss) {
      Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 6.dp,
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 18.dp)
        ) {
          Text(
            modifier = Modifier
              .fillMaxWidth()
              .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
            maxLines = 1,
            text = stringResource(R.string.create_book),
            color =  MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall
          )
          CreateBookSheetItem(
            icon = Icons.Outlined.QrCodeScanner,
            text = stringResource(R.string.action_scan_barcode),
            onClick = {
              onScanBarcodeClick()
              onDismiss()
            }
          )
          CreateBookSheetItem(
            icon = Icons.Outlined.Search,
            text = stringResource(R.string.action_search_by_isbn),
            onClick = {
              onIsbnSearchClick()
              onDismiss()
            }
          )
          CreateBookSheetItem(
            icon = Icons.Outlined.EditNote,
            text = stringResource(R.string.action_fill_manually),
            onClick = {
              onFillManuallyClick()
              onDismiss()
            }
          )
        }
      }
    }
  }

  @Composable
  fun CreateBookSheetItem(
    icon: ImageVector,
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
        imageVector = icon,
        contentDescription = text,
        tint = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }

}
