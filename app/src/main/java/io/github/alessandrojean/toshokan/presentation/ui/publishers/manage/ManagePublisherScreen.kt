package io.github.alessandrojean.toshokan.presentation.ui.publishers.manage

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.Publisher

@Composable
fun ManagePublisherScreen(
  mode: ManagePublisherMode,
  publisher: Publisher? = null,
  onClose: () -> Unit = {},
  managePublisherViewModel: ManagePublisherViewModel
) {
  val uiState by managePublisherViewModel.uiState.collectAsState()
  val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior() }
  val listState = rememberLazyListState()
  val formInvalid by remember {
    derivedStateOf {
      uiState.name.isEmpty()
    }
  }

  if (publisher != null && mode == ManagePublisherMode.EDIT) {
    managePublisherViewModel.setFieldValues(publisher)
  }

  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      SmallTopAppBar(
        scrollBehavior = scrollBehavior,
        navigationIcon = {
          IconButton(
            enabled = !uiState.writing,
            onClick = onClose
          ) {
            Icon(
              Icons.Default.Close,
              contentDescription = stringResource(R.string.action_back)
            )
          }
        },
        title = {
          Text(
            text = if (mode == ManagePublisherMode.CREATE) {
              stringResource(R.string.create_publisher)
            } else {
              publisher!!.name
            }
          )
        },
        actions = {
          TextButton(
            enabled = !uiState.writing,
            onClick = {
              if (!formInvalid) {
                if (mode == ManagePublisherMode.CREATE) {
                  managePublisherViewModel.create()
                } else {
                  managePublisherViewModel.edit()
                }

                onClose()
              }
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
            enabled = !uiState.writing,
            value = uiState.name,
            onValueChange = { managePublisherViewModel.onNameChanged(it) },
            singleLine = true,
            label = { Text(stringResource(R.string.name)) }
          )
        }

        item {
          OutlinedTextField(
            modifier = Modifier
              .fillMaxWidth()
              .height(120.dp),
            enabled = !uiState.writing,
            value = uiState.description,
            onValueChange = { managePublisherViewModel.onDescriptionChanged(it) },
            label = { Text(stringResource(R.string.description)) }
          )
        }

        item {
          OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.writing,
            value = uiState.website,
            onValueChange = { managePublisherViewModel.onWebsiteChanged(it) },
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
            enabled = !uiState.writing,
            value = uiState.instagramProfile,
            onValueChange = { managePublisherViewModel.onInstagramProfileChanged(it) },
            singleLine = true,
            label = { Text(stringResource(R.string.instagram)) }
          )
        }

        item {
          OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.writing,
            value = uiState.twitterProfile,
            onValueChange = { managePublisherViewModel.onTwitterProfileChanged(it) },
            singleLine = true,
            label = { Text(stringResource(R.string.twitter)) }
          )
        }
      }
    }
  )
}