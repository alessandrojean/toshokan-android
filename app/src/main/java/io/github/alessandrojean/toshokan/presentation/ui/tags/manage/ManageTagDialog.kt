package io.github.alessandrojean.toshokan.presentation.ui.tags.manage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.Icon
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.Tag
import io.github.alessandrojean.toshokan.presentation.ui.core.dialog.DialogHorizontalPadding
import io.github.alessandrojean.toshokan.presentation.ui.core.dialog.EnhancedAlertDialog
import io.github.alessandrojean.toshokan.presentation.ui.core.dialog.ItemOption
import kotlinx.coroutines.android.awaitFrame

@Composable
fun ManageTagDialog(
  mode: ManageTagMode,
  tag: Tag? = null,
  onClose: () -> Unit = {},
  manageTagViewModel: ManageTagViewModel
) {

  val focusRequester = remember { FocusRequester() }

  if (tag != null && mode == ManageTagMode.EDIT) {
    manageTagViewModel.setFieldValues(tag)
  }

  EnhancedAlertDialog(
    onDismissRequest = {
      manageTagViewModel.clearFields()
      onClose()
    },
    icon = {
      Icon(
        imageVector = Icons.Outlined.Label,
        contentDescription = null
      )
    },
    title = {
      Text(
        text = if (mode == ManageTagMode.CREATE) {
          stringResource(R.string.create_tag)
        } else {
          tag!!.name
        }
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          modifier = Modifier
            .fillMaxWidth()
            .padding(DialogHorizontalPadding)
            .focusRequester(focusRequester),
          enabled = !manageTagViewModel.writing,
          value = manageTagViewModel.name,
          onValueChange = { manageTagViewModel.name = it },
          singleLine = true,
          label = { Text(stringResource(R.string.name)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        ItemOption(
          modifier = Modifier.fillMaxWidth(),
          text = stringResource(R.string.tag_nsfw),
          role = Role.Checkbox,
          selected = manageTagViewModel.isNsfw,
          onClick = { manageTagViewModel.isNsfw = !manageTagViewModel.isNsfw }
        )
      }
    },
    dismissButton = {
      TextButton(
        onClick = {
          manageTagViewModel.clearFields()
          onClose()
        }
      ) {
        Text(stringResource(R.string.action_cancel))
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          if (mode == ManageTagMode.CREATE) {
            manageTagViewModel.create().invokeOnCompletion { onClose() }
          } else {
            manageTagViewModel.edit().invokeOnCompletion { onClose() }
          }
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