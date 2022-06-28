package io.github.alessandrojean.toshokan.presentation.ui.core.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.R
import kotlinx.coroutines.android.awaitFrame

@Composable
fun SearchTopAppBar(
  modifier: Modifier = Modifier,
  backgroundColor: Color = MaterialTheme.colorScheme.surface,
  searchText: String = "",
  placeholderText: String = "",
  scrollBehavior: TopAppBarScrollBehavior,
  shouldRequestFocus: Boolean = true,
  onNavigationClick: () -> Unit,
  onClearClick: () -> Unit = {},
  onSearchTextChanged: (String) -> Unit = {},
  onSearchAction: () -> Unit = {},
  bottomContent: @Composable ColumnScope.() -> Unit = {},
  actions: @Composable RowScope.() -> Unit = {},
  keyboardType: KeyboardType = KeyboardType.Text
) {
  val keyboardController = LocalSoftwareKeyboardController.current
  val focusRequester = remember { FocusRequester() }
  val focusManager = LocalFocusManager.current

  Surface(
    color = backgroundColor,
    modifier = modifier
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      SmallTopAppBar(
        modifier = Modifier.statusBarsPadding(),
        colors = TopAppBarDefaults.smallTopAppBarColors(
          containerColor = Color.Transparent,
          scrolledContainerColor = Color.Transparent
        ),
        scrollBehavior = scrollBehavior,
        navigationIcon = {
          IconButton(onClick = onNavigationClick) {
            Icon(
              Icons.Default.ArrowBack,
              contentDescription = stringResource(R.string.action_back)
            )
          }
        },
        title = {
          OutlinedTextField(
            modifier = Modifier
              .fillMaxWidth()
              .offset(x = (-16).dp)
              .focusRequester(focusRequester)
              .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                  keyboardController?.show()
                }
              },
            value = searchText,
            onValueChange = onSearchTextChanged,
            placeholder = {
              Text(
                text = placeholderText,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            },
            colors = TextFieldDefaults.textFieldColors(
              focusedIndicatorColor = Color.Transparent,
              unfocusedIndicatorColor = Color.Transparent,
              containerColor = Color.Transparent,
              cursorColor = LocalContentColor.current
            ),
            textStyle = MaterialTheme.typography.titleLarge.copy(
              color = LocalContentColor.current
            ),
            maxLines = 1,
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
              imeAction = ImeAction.Search,
              keyboardType = keyboardType
            ),
            keyboardActions = KeyboardActions(
              onSearch = {
                focusManager.clearFocus()
                onSearchAction()
              }
            )
          )
        },
        actions = {
          AnimatedVisibility(
            visible = searchText.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
          ) {
            IconButton(onClick = { onClearClick() }) {
              Icon(
                imageVector = Icons.Outlined.Cancel,
                contentDescription = stringResource(R.string.action_clear),
                tint = LocalContentColor.current
              )
            }
          }

          actions.invoke(this)
        }
      )

      bottomContent.invoke(this)
    }
  }

  LaunchedEffect(Unit) {
    if (shouldRequestFocus) {
      awaitFrame()
      focusRequester.requestFocus()
    }
  }
}