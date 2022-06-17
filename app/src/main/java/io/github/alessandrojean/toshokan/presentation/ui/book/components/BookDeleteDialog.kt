package io.github.alessandrojean.toshokan.presentation.ui.book.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import io.github.alessandrojean.toshokan.R

@Composable
fun BookDeleteDialog(
  visible: Boolean,
  onDismiss: () -> Unit,
  onDelete: () -> Unit
) {
  if (visible) {
    AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text(stringResource(R.string.book_delete_title)) },
      text = { Text(pluralStringResource(R.plurals.book_delete_warning, count = 1)) },
      confirmButton = {
        TextButton(
          onClick = {
            onDelete.invoke()
            onDismiss.invoke()
          }
        ) {
          Text(stringResource(R.string.action_delete))
        }
      },
      dismissButton = {
        TextButton(onClick = onDismiss) {
          Text(stringResource(R.string.action_cancel))
        }
      }
    )
  }
}
