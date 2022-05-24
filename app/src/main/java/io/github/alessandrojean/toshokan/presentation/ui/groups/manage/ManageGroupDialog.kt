package io.github.alessandrojean.toshokan.presentation.ui.groups.manage

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.BookGroup
import kotlinx.coroutines.android.awaitFrame

@Composable
fun ManageGroupDialog(
  mode: ManageGroupMode,
  group: BookGroup? = null,
  onClose: () -> Unit = {},
  manageGroupViewModel: ManageGroupViewModel
) {

  val focusRequester = remember { FocusRequester() }

  if (group != null && mode == ManageGroupMode.EDIT) {
    manageGroupViewModel.setFieldValues(group)
  }

  AlertDialog(
    onDismissRequest = {
      manageGroupViewModel.clearFields()
      onClose()
    },
    title = {
      Text(
        text = if (mode == ManageGroupMode.CREATE) {
          stringResource(R.string.create_group)
        } else {
          group!!.name
        }
      )
    },
    text = {
      OutlinedTextField(
        modifier = Modifier
          .fillMaxWidth()
          .focusRequester(focusRequester),
        enabled = !manageGroupViewModel.writing,
        value = manageGroupViewModel.name,
        onValueChange = { manageGroupViewModel.name = it },
        singleLine = true,
        label = { Text(stringResource(R.string.name)) }
      )
    },
    dismissButton = {
      TextButton(
        onClick = {
          manageGroupViewModel.clearFields()
          onClose()
        }
      ) {
        Text(stringResource(R.string.action_cancel))
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          if (mode == ManageGroupMode.CREATE) {
            manageGroupViewModel.create()
          } else {
            manageGroupViewModel.edit()
          }

          onClose()
        }
      ) {
        Text(stringResource(R.string.action_finish))
      }
    }
  )

  LaunchedEffect(Unit) {
    awaitFrame()
    focusRequester.requestFocus()
  }
}