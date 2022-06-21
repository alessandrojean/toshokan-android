package io.github.alessandrojean.toshokan.presentation.ui.publishers.manage

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.Publisher
import io.github.alessandrojean.toshokan.presentation.ui.core.components.EnhancedSmallTopAppBar

class ManagePublisherScreen(
  private val mode: ManagePublisherMode = ManagePublisherMode.CREATE,
  private val publisher: Publisher? = null
) : AndroidScreen() {

  @Composable
  override fun Content() {
    val managePublisherViewModel = getViewModel<ManagePublisherViewModel>()
    val navigator = LocalNavigator.currentOrThrow
    val topAppBarScrollState = rememberTopAppBarScrollState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior(topAppBarScrollState) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val websiteInvalid by remember {
      derivedStateOf {
        managePublisherViewModel.website.isNotBlank() &&
          !Patterns.WEB_URL.matcher(managePublisherViewModel.website).matches()
      }
    }

    LaunchedEffect(Unit) {
      if (publisher != null && mode == ManagePublisherMode.EDIT) {
        managePublisherViewModel.setFieldValues(publisher)
      }
    }

    Scaffold(
      modifier = Modifier
        .nestedScroll(scrollBehavior.nestedScrollConnection)
        .navigationBarsPadding(),
      topBar = {
        EnhancedSmallTopAppBar(
          contentPadding = WindowInsets.statusBars.asPaddingValues(),
          scrollBehavior = scrollBehavior,
          navigationIcon = {
            IconButton(
              enabled = !managePublisherViewModel.writing,
              onClick = { navigator.pop() }
            ) {
              Icon(
                Icons.Default.ArrowBack,
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
              enabled = !managePublisherViewModel.writing,
              onClick = {
                if (mode == ManagePublisherMode.CREATE) {
                  managePublisherViewModel.create { navigator.pop() }
                } else {
                  managePublisherViewModel.edit { navigator.pop() }
                }
              },
              content = { Text(stringResource(R.string.action_finish)) }
            )
          }
        )
      },
      content = { innerPadding ->
        Box(
          modifier = Modifier
            .padding(innerPadding)
            .imePadding()
        ) {
          // TODO: Change to LazyColumn when the focus issue gets fixed.
          // Ref: https://issuetracker.google.com/issues/179203700
          // TODO: Handle focus on text fields when scroll.
          Column(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState())
              .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top)
          ) {
            OutlinedTextField(
              modifier = Modifier.fillMaxWidth(),
              value = managePublisherViewModel.name,
              isError = managePublisherViewModel.name.isBlank(),
              onValueChange = { managePublisherViewModel.name = it },
              singleLine = true,
              label = { Text(stringResource(R.string.name)) },
              keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
              keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
              )
            )

            OutlinedTextField(
              modifier = Modifier.fillMaxWidth(),
              value = managePublisherViewModel.description,
              maxLines = 10,
              onValueChange = { managePublisherViewModel.description = it },
              label = { Text(stringResource(R.string.description)) }
            )

            OutlinedTextField(
              modifier = Modifier.fillMaxWidth(),
              value = managePublisherViewModel.website,
              isError = websiteInvalid,
              onValueChange = { managePublisherViewModel.website = it },
              singleLine = true,
              label = { Text(stringResource(R.string.website)) },
              keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Next
              ),
              keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
              )
            )

            OutlinedTextField(
              modifier = Modifier.fillMaxWidth(),
              value = managePublisherViewModel.instagramProfile,
              onValueChange = { managePublisherViewModel.instagramProfile = it },
              singleLine = true,
              label = { Text(stringResource(R.string.instagram)) },
              keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
              keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
              )
            )

            OutlinedTextField(
              modifier = Modifier.fillMaxWidth(),
              value = managePublisherViewModel.twitterProfile,
              onValueChange = { managePublisherViewModel.twitterProfile = it },
              singleLine = true,
              label = { Text(stringResource(R.string.twitter)) },
              keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
              keyboardActions = KeyboardActions(
                onDone = {
                  focusManager.clearFocus()
                  keyboardController?.hide()
                }
              )
            )
          }
        }
      }
    )
  }

}
