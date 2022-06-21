package io.github.alessandrojean.toshokan.presentation.ui.book.reading

import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkAdded
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Divider
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.android.material.datepicker.MaterialDatePicker
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.Reading
import io.github.alessandrojean.toshokan.presentation.ui.core.components.EnhancedSmallTopAppBar
import io.github.alessandrojean.toshokan.presentation.ui.core.components.NoItemsFound
import io.github.alessandrojean.toshokan.presentation.ui.core.components.SelectionTopAppBar
import io.github.alessandrojean.toshokan.presentation.ui.core.picker.showDatePicker
import io.github.alessandrojean.toshokan.presentation.ui.theme.DividerOpacity
import io.github.alessandrojean.toshokan.presentation.ui.theme.ModalBottomSheetExtraLargeShape
import io.github.alessandrojean.toshokan.util.extension.collectAsStateWithLifecycle
import io.github.alessandrojean.toshokan.util.extension.formatToLocaleDate
import io.github.alessandrojean.toshokan.util.extension.toLocalCalendar
import kotlinx.coroutines.launch

class ReadingScreen(val bookId: Long) : AndroidScreen() {

  @Composable
  override fun Content() {
    val readingScreenModel = getScreenModel<ReadingScreenModel, ReadingScreenModel.Factory> { factory ->
      factory.create(bookId)
    }
    val readings by readingScreenModel.readings.collectAsStateWithLifecycle(emptyList())
    val navigator = LocalNavigator.currentOrThrow
    val activity = LocalContext.current as AppCompatActivity
    val scope = rememberCoroutineScope()
    val dialogTitle = stringResource(R.string.read_at)

    val topAppBarScrollState = rememberTopAppBarScrollState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior(topAppBarScrollState) }
    val listState = rememberLazyListState()

    val modalBottomSheetState = rememberModalBottomSheetState(
      initialValue = ModalBottomSheetValue.Hidden,
      skipHalfExpanded = true
    )

    BackHandler(enabled = readingScreenModel.selectionMode) {
      readingScreenModel.clearSelection()
    }

    val fabExpanded by remember {
      derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }

    ModalBottomSheetLayout(
      modifier = Modifier.fillMaxSize(),
      sheetState = modalBottomSheetState,
      sheetShape = ModalBottomSheetExtraLargeShape,
      sheetBackgroundColor = Color.Transparent,
      sheetContent = {
        ModalBottomSheetContent(
          modifier = Modifier.fillMaxWidth(),
          onTodayDateClick = {
            scope.launch { modalBottomSheetState.hide() }
            readingScreenModel.createReading(
              MaterialDatePicker.todayInUtcMilliseconds()
                .toLocalCalendar()?.timeInMillis
            )
          },
          onOtherDateClick = {
            scope.launch { modalBottomSheetState.hide() }
            showDatePicker(
              activity = activity,
              titleText = dialogTitle,
              onDateChoose = { readingScreenModel.createReading(it) }
            )
          },
          onUnknownDateClick = {
            scope.launch { modalBottomSheetState.hide() }
            readingScreenModel.createReading(null)
          }
        )
      }
    ) {
      Scaffold(
        modifier = Modifier
          .nestedScroll(scrollBehavior.nestedScrollConnection)
          .navigationBarsPadding(),
        topBar = {
          Crossfade(targetState = readingScreenModel.selectionMode) { selectionMode ->
            if (selectionMode) {
              SelectionTopAppBar(
                selectionCount = readingScreenModel.selection.size,
                onClearSelectionClick = { readingScreenModel.clearSelection() },
                onDeleteClick = { readingScreenModel.deleteSelection() },
                scrollBehavior = scrollBehavior
              )
            } else {
              EnhancedSmallTopAppBar(
                contentPadding = WindowInsets.statusBars.asPaddingValues(),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                  IconButton(onClick = { navigator.pop() }) {
                    Icon(
                      imageVector = Icons.Outlined.ArrowBack,
                      contentDescription = stringResource(R.string.action_back)
                    )
                  }
                },
                title = {
                  Text(
                    text = stringResource(R.string.readings),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              )
            }
          }
        },
        floatingActionButton = {
          AnimatedVisibility(
            visible = !readingScreenModel.selectionMode,
            enter = fadeIn(),
            exit = fadeOut()
          ) {
            ExtendedFloatingActionButton(
              expanded = fabExpanded,
              text = { Text(stringResource(R.string.create_reading)) },
              icon = {
                Icon(
                  imageVector = Icons.Outlined.Add,
                  contentDescription = null
                )
              },
              onClick = {
                scope.launch { modalBottomSheetState.show() }
              }
            )
          }
        },
        content = { innerPadding ->
          Crossfade(
            modifier = Modifier.fillMaxSize(),
            targetState = readings.isEmpty()
          ) { isEmpty ->
            if (isEmpty) {
              NoItemsFound(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(innerPadding),
                text = stringResource(R.string.no_readings),
                icon = Icons.Outlined.Bookmarks
              )
            } else {
              LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = innerPadding,
                state = listState
              ) {
                items(readings) { reading ->
                  ReadingItem(
                    modifier = Modifier
                      .fillMaxWidth()
                      .animateItemPlacement(),
                    reading = reading,
                    selected = reading.id in readingScreenModel.selection,
                    onClick = {
                      if (readingScreenModel.selectionMode) {
                        readingScreenModel.toggleSelection(reading.id)
                      }
                    },
                    onLongClick = {
                      readingScreenModel.toggleSelection(reading.id)
                    }
                  )
                }
              }
            }
          }
        }
      )
    }
  }

  @Composable
  fun ModalBottomSheetContent(
    modifier: Modifier = Modifier,
    onTodayDateClick: () -> Unit,
    onOtherDateClick: () -> Unit,
    onUnknownDateClick: () -> Unit
  ) {
    Surface(
      modifier = modifier,
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      shape = ModalBottomSheetExtraLargeShape,
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
      ) {
        Text(
          modifier = Modifier.padding(top = 24.dp, start = 24.dp, end = 24.dp),
          text = stringResource(R.string.read_at),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          style = MaterialTheme.typography.titleLarge
        )
        Divider(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
          color = LocalContentColor.current.copy(alpha = DividerOpacity)
        )
        ModalBottomSheetItem(
          text = stringResource(R.string.today),
          icon = Icons.Outlined.Today,
          onClick = onTodayDateClick
        )
        ModalBottomSheetItem(
          text = stringResource(R.string.other_date),
          icon = Icons.Outlined.Event,
          onClick = onOtherDateClick
        )
        ModalBottomSheetItem(
          text = stringResource(R.string.unknown),
          icon = Icons.Outlined.QuestionMark,
          onClick = onUnknownDateClick
        )
      }
    }
  }

  @Composable
  fun ModalBottomSheetItem(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
  ) {
    Row(
      modifier = Modifier
        .clickable(onClick = onClick)
        .padding(vertical = 16.dp, horizontal = 24.dp)
        .fillMaxWidth()
        .then(modifier),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null
      )
      Text(
        modifier = Modifier.padding(start = 24.dp),
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }

  @Composable
  fun ReadingItem(
    modifier: Modifier = Modifier,
    reading: Reading,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
  ) {
    Row(
      modifier = Modifier
        .combinedClickable(
          onClick = onClick,
          onLongClick = onLongClick
        )
        .background(
          if (selected) MaterialTheme.colorScheme.surfaceVariant
          else MaterialTheme.colorScheme.surface
        )
        .padding(16.dp)
        .then(modifier),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Outlined.BookmarkAdded,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Text(
        modifier = Modifier.padding(start = 16.dp),
        text = reading.read_at?.formatToLocaleDate() ?: stringResource(R.string.unknown),
        maxLines = 1
      )
    }
  }

}