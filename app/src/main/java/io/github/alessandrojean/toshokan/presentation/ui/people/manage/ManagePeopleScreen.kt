package io.github.alessandrojean.toshokan.presentation.ui.people.manage

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.ManageSearch
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
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
import io.github.alessandrojean.toshokan.database.data.Person
import io.github.alessandrojean.toshokan.presentation.ui.core.components.EnhancedSmallTopAppBar
import io.github.alessandrojean.toshokan.presentation.ui.core.dialog.FullScreenItemPickerDialog
import io.github.alessandrojean.toshokan.util.extension.toCountryDisplayName
import io.github.alessandrojean.toshokan.util.extension.toFlagEmoji
import java.text.Collator
import java.util.Locale

class ManagePeopleScreen(
  private val mode: ManagePeopleMode = ManagePeopleMode.CREATE,
  private val person: Person? = null
) : AndroidScreen() {

  @Composable
  override fun Content() {
    val managePeopleViewModel = getViewModel<ManagePeopleViewModel>()
    val navigator = LocalNavigator.currentOrThrow
    val topAppBarScrollState = rememberTopAppBarScrollState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior(topAppBarScrollState) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val currentLocale = Locale.getDefault()
    val collator = Collator.getInstance(currentLocale)
    var showCountryPickerDialog by remember { mutableStateOf(false) }

    val countries = remember {
      Locale.getISOCountries()
        .map { it to it.toCountryDisplayName(currentLocale) }
        .sortedWith(compareBy(collator) { it.second })
    }

    val websiteInvalid by remember {
      derivedStateOf {
        managePeopleViewModel.website.isNotBlank() &&
          !Patterns.WEB_URL.matcher(managePeopleViewModel.website).matches()
      }
    }

    LaunchedEffect(Unit) {
      if (person != null && mode == ManagePeopleMode.EDIT) {
        managePeopleViewModel.setFieldValues(person)
      }
    }

    FullScreenItemPickerDialog<Pair<String, String>>(
      visible = showCountryPickerDialog,
      title = stringResource(R.string.country),
      selected = countries.firstOrNull { it.first == managePeopleViewModel.country },
      items = countries,
      initialSearch = managePeopleViewModel.country.toCountryDisplayName(currentLocale),
      itemKey = { it.first },
      itemText = { it.second },
      itemTrailingIcon = { Text(it.first.toFlagEmoji()) },
      onChoose = { managePeopleViewModel.country = it.first },
      onDismiss = { showCountryPickerDialog = false },
      nullable = true,
      onClear = { managePeopleViewModel.country = "" }
    )

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
              enabled = !managePeopleViewModel.writing,
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
              text = if (mode == ManagePeopleMode.CREATE) {
                stringResource(R.string.create_person)
              } else {
                person!!.name
              }
            )
          },
          actions = {
            TextButton(
              enabled = !managePeopleViewModel.writing,
              onClick = {
                if (mode == ManagePeopleMode.CREATE) {
                  managePeopleViewModel.create { navigator.pop() }
                } else {
                  managePeopleViewModel.edit { navigator.pop() }
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
              value = managePeopleViewModel.name,
              onValueChange = { managePeopleViewModel.name = it },
              singleLine = true,
              isError = managePeopleViewModel.name.isBlank(),
              label = { Text(stringResource(R.string.name)) },
              keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
              keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
              )
            )

            OutlinedTextField(
              modifier = Modifier.fillMaxWidth(),
              value = managePeopleViewModel.description,
              maxLines = 10,
              onValueChange = { managePeopleViewModel.description = it },
              label = { Text(stringResource(R.string.description)) }
            )

            OutlinedTextField(
              modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                  if (it.isFocused) {
                    showCountryPickerDialog = true
                  }
                },
              readOnly = true,
              value = managePeopleViewModel.country.toCountryDisplayName(currentLocale),
              onValueChange = {},
              singleLine = true,
              label = { Text(stringResource(R.string.country)) },
              trailingIcon = {
                IconButton(onClick = { showCountryPickerDialog = true }) {
                  Icon(
                    imageVector = Icons.Outlined.ManageSearch,
                    contentDescription = stringResource(R.string.action_search)
                  )
                }
              },
              keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
              keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
              )
            )

            OutlinedTextField(
              modifier = Modifier.fillMaxWidth(),
              value = managePeopleViewModel.website,
              onValueChange = { managePeopleViewModel.website = it },
              singleLine = true,
              isError = websiteInvalid,
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
              value = managePeopleViewModel.instagramProfile,
              onValueChange = { managePeopleViewModel.instagramProfile = it },
              singleLine = true,
              label = { Text(stringResource(R.string.instagram)) },
              keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
              keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
              )
            )

            OutlinedTextField(
              modifier = Modifier.fillMaxWidth(),
              value = managePeopleViewModel.twitterProfile,
              onValueChange = { managePeopleViewModel.twitterProfile = it },
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