package io.github.alessandrojean.toshokan.presentation.ui.stores.manage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.Store

@Composable
fun ManageStoreDialog(
  mode: ManageStoreMode,
  store: Store? = null,
  onClose: () -> Unit = {},
  manageStoreViewModel: ManageStoreViewModel
) {
  val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior() }
  val listState = rememberLazyListState()

  if (store != null && mode == ManageStoreMode.EDIT) {
    manageStoreViewModel.setFieldValues(store)
  }

  Dialog(
    onDismissRequest = {
      manageStoreViewModel.clearFields()
      onClose()
    },
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(modifier = Modifier.fillMaxWidth()) {
      Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
          SmallTopAppBar(
            scrollBehavior = scrollBehavior,
            navigationIcon = {
              IconButton(
                enabled = !manageStoreViewModel.writing,
                onClick = {
                  manageStoreViewModel.clearFields()
                  onClose()
                }
              ) {
                Icon(
                  Icons.Default.Close,
                  contentDescription = stringResource(R.string.action_back)
                )
              }
            },
            title = {
              Text(
                text = if (mode == ManageStoreMode.CREATE) {
                  stringResource(R.string.create_store)
                } else {
                  store!!.name
                }
              )
            },
            actions = {
              TextButton(
                enabled = !manageStoreViewModel.writing,
                onClick = {
                  if (mode == ManageStoreMode.CREATE) {
                    manageStoreViewModel.create()
                  } else {
                    manageStoreViewModel.edit()
                  }

                  onClose()
                },
                content = { Text(stringResource(R.string.action_finish)) }
              )
            }
          )
        },
        content = { innerPadding ->
          LazyColumn(
            state = listState,
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top)
          ) {
            item {
              OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                enabled = !manageStoreViewModel.writing,
                value = manageStoreViewModel.name,
                onValueChange = { manageStoreViewModel.name = it },
                singleLine = true,
                label = { Text(stringResource(R.string.name)) }
              )
            }

            item {
              OutlinedTextField(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(120.dp),
                enabled = !manageStoreViewModel.writing,
                value = manageStoreViewModel.description,
                onValueChange = { manageStoreViewModel.description = it },
                label = { Text(stringResource(R.string.description)) }
              )
            }

            item {
              OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                enabled = !manageStoreViewModel.writing,
                value = manageStoreViewModel.website,
                onValueChange = { manageStoreViewModel.website = it },
                singleLine = true,
                label = { Text(stringResource(R.string.website)) },
                keyboardOptions = KeyboardOptions.Default.copy(
                  keyboardType = KeyboardType.Uri
                )
              )
            }

            item {
              OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                enabled = !manageStoreViewModel.writing,
                value = manageStoreViewModel.instagramProfile,
                onValueChange = { manageStoreViewModel.instagramProfile = it },
                singleLine = true,
                label = { Text(stringResource(R.string.instagram)) }
              )
            }

            item {
              OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                enabled = !manageStoreViewModel.writing,
                value = manageStoreViewModel.twitterProfile,
                onValueChange = { manageStoreViewModel.twitterProfile = it },
                singleLine = true,
                label = { Text(stringResource(R.string.twitter)) }
              )
            }
          }
        }
      )
    }
  }
}