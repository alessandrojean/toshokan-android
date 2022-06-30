package io.github.alessandrojean.toshokan.presentation.ui.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldDefaults.indicatorLine
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.extensions.withTonalElevation
import kotlinx.coroutines.android.awaitFrame

@Composable
fun SearchTopAppBar(
  modifier: Modifier = Modifier,
  containerColor: Color = MaterialTheme.colorScheme.surface,
  textFieldContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant.withTonalElevation(2.dp),
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
  var textFieldState by remember { mutableStateOf(TextFieldValue(searchText)) }

  LaunchedEffect(searchText) {
    textFieldState = textFieldState.copy(text = searchText)
  }

  SearchTopAppBar(
    modifier = modifier,
    containerColor = containerColor,
    textFieldContainerColor = textFieldContainerColor,
    searchText = textFieldState,
    placeholderText = placeholderText,
    scrollBehavior = scrollBehavior,
    shouldRequestFocus = shouldRequestFocus,
    onNavigationClick = onNavigationClick,
    onClearClick = onClearClick,
    onSearchTextChanged = {
      textFieldState = it
      onSearchTextChanged(it.text)
    },
    onSearchAction = onSearchAction,
    bottomContent = bottomContent,
    actions = actions,
    keyboardType = keyboardType
  )
}

@Composable
fun SearchTopAppBar(
  modifier: Modifier = Modifier,
  containerColor: Color = MaterialTheme.colorScheme.surface,
  textFieldContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant.withTonalElevation(2.dp),
  searchText: TextFieldValue = TextFieldValue(""),
  placeholderText: String = "",
  scrollBehavior: TopAppBarScrollBehavior,
  shouldRequestFocus: Boolean = true,
  onNavigationClick: () -> Unit,
  onClearClick: () -> Unit = {},
  onSearchTextChanged: (TextFieldValue) -> Unit = {},
  onSearchAction: () -> Unit = {},
  bottomContent: @Composable ColumnScope.() -> Unit = {},
  actions: @Composable RowScope.() -> Unit = {},
  keyboardType: KeyboardType = KeyboardType.Text,
  focusRequester: FocusRequester = remember { FocusRequester() }
) {
  val keyboardController = LocalSoftwareKeyboardController.current
  val focusManager = LocalFocusManager.current

  Surface(
    color = containerColor,
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
          EnhancedTextField(
            modifier = Modifier
              .fillMaxWidth()
              .height(42.dp)
              .focusRequester(focusRequester)
              .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                  keyboardController?.show()
                }
              },
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 10.dp),
            shape = MaterialTheme.shapes.medium,
            value = searchText,
            onValueChange = onSearchTextChanged,
            placeholder = {
              Text(
                text = placeholderText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge
              )
            },
            colors = TextFieldDefaults.textFieldColors(
              focusedIndicatorColor = Color.Transparent,
              unfocusedIndicatorColor = Color.Transparent,
              containerColor = textFieldContainerColor,
              cursorColor = LocalContentColor.current
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
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
            ),
            trailingIcon = if (searchText.text.isNotEmpty()) {
              {
                IconButton(
                  onClick = {
                    onClearClick()
                    focusRequester.requestFocus()
                  }
                ) {
                  Icon(
                    modifier = Modifier.size(20.dp),
                    painter = rememberVectorPainter(Icons.Outlined.Cancel),
                    contentDescription = stringResource(R.string.action_clear),
                    tint = LocalContentColor.current
                  )
                }
              }
            } else null
          )
        },
        actions = actions
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

@Composable
fun EnhancedTextField(
  value: TextFieldValue,
  onValueChange: (TextFieldValue) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  readOnly: Boolean = false,
  textStyle: TextStyle = LocalTextStyle.current,
  label: @Composable (() -> Unit)? = null,
  placeholder: @Composable (() -> Unit)? = null,
  leadingIcon: @Composable (() -> Unit)? = null,
  trailingIcon: @Composable (() -> Unit)? = null,
  isError: Boolean = false,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  singleLine: Boolean = false,
  maxLines: Int = Int.MAX_VALUE,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  shape: Shape = MaterialTheme.shapes.medium,
  colors: TextFieldColors = TextFieldDefaults.textFieldColors(),
  contentPadding: PaddingValues = PaddingValues(16.dp)
) {
  // If color is not provided via the text style, use content color as a default
  val textColor = textStyle.color.takeOrElse {
    colors.textColor(enabled).value
  }
  val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

  @OptIn(ExperimentalMaterial3Api::class)
  BasicTextField(
    value = value,
    modifier = modifier
      .background(colors.containerColor(enabled).value, shape)
      .indicatorLine(enabled, isError, interactionSource, colors)
      .defaultMinSize(
        minWidth = TextFieldDefaults.MinWidth,
        minHeight = TextFieldDefaults.MinHeight
      ),
    onValueChange = onValueChange,
    enabled = enabled,
    readOnly = readOnly,
    textStyle = mergedTextStyle,
    cursorBrush = SolidColor(colors.cursorColor(isError).value),
    visualTransformation = visualTransformation,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    interactionSource = interactionSource,
    singleLine = singleLine,
    maxLines = maxLines,
    decorationBox = @Composable { innerTextField ->
      // places leading icon, text field with label and placeholder, trailing icon
      TextFieldDefaults.TextFieldDecorationBox(
        value = value.text,
        visualTransformation = visualTransformation,
        innerTextField = innerTextField,
        placeholder = placeholder,
        label = label,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        enabled = enabled,
        isError = isError,
        interactionSource = interactionSource,
        colors = colors,
        contentPadding = contentPadding
      )
    }
  )
}