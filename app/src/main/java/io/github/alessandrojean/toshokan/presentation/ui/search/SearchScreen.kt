package io.github.alessandrojean.toshokan.presentation.ui.search

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Badge
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.util.Pair
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.Book
import io.github.alessandrojean.toshokan.domain.DateRange
import io.github.alessandrojean.toshokan.presentation.extensions.surfaceWithTonalElevation
import io.github.alessandrojean.toshokan.presentation.ui.book.BookScreen
import io.github.alessandrojean.toshokan.presentation.ui.core.components.BookCard
import io.github.alessandrojean.toshokan.presentation.ui.core.components.NoItemsFound
import io.github.alessandrojean.toshokan.presentation.ui.core.components.SearchTopAppBar
import io.github.alessandrojean.toshokan.presentation.ui.core.dialog.FullScreenItemPickerDialog
import io.github.alessandrojean.toshokan.presentation.ui.theme.DividerOpacity
import io.github.alessandrojean.toshokan.presentation.ui.theme.ModalBottomSheetLargeShape
import io.github.alessandrojean.toshokan.util.extension.collectAsStateWithLifecycle

class SearchScreen : AndroidScreen() {

  @Composable
  override fun Content() {
    val viewModel = getViewModel<SearchViewModel>()
    val navigator = LocalNavigator.currentOrThrow
    val activity = LocalContext.current as AppCompatActivity
    val topAppBarScrollState = rememberTopAppBarScrollState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior(topAppBarScrollState) }

    val systemUiController = rememberSystemUiController()
    val navigationBarColor = MaterialTheme.colorScheme.surfaceWithTonalElevation(6.dp)

    LifecycleEffect(
      onStarted = {
        systemUiController.setNavigationBarColor(
          color = navigationBarColor
        )
      }
    )

    val topAppBarBackgroundColors = TopAppBarDefaults.smallTopAppBarColors()
    val topAppBarBackground = topAppBarBackgroundColors.containerColor(scrollBehavior.scrollFraction).value

    val allGroups by viewModel.allGroups.collectAsStateWithLifecycle(emptyList())
    val allPersons by viewModel.allPersons.collectAsStateWithLifecycle(emptyList())
    val allPublishers by viewModel.allPublishers.collectAsStateWithLifecycle(emptyList())
    val allStores by viewModel.allStores.collectAsStateWithLifecycle(emptyList())

    var showGroupsPickerDialog by remember { mutableStateOf(false) }
    var showContributorsPickerDialog by remember { mutableStateOf(false) }
    var showPublishersPickerDialog by remember { mutableStateOf(false) }
    var showStoresPickerDialog by remember { mutableStateOf(false) }

    val boughtAtPickerTitle = stringResource(R.string.filter_bought_at)
    val readAtPickerTitle = stringResource(R.string.filter_read_at)

    FullScreenItemPickerDialog(
      role = Role.Checkbox,
      nullable = true,
      visible = showGroupsPickerDialog,
      title = stringResource(R.string.groups),
      selected = viewModel.filters.groups,
      items = allGroups,
      itemKey = { it.id },
      itemText = { it.name },
      onChoose = { viewModel.onGroupsChanged(it) },
      onDismiss = { showGroupsPickerDialog = false }
    )

    FullScreenItemPickerDialog(
      role = Role.Checkbox,
      nullable = true,
      visible = showPublishersPickerDialog,
      title = stringResource(R.string.publishers),
      selected = viewModel.filters.publishers,
      items = allPublishers,
      itemKey = { it.id },
      itemText = { it.name },
      onChoose = { viewModel.onPublishersChanged(it) },
      onDismiss = { showPublishersPickerDialog = false }
    )

    FullScreenItemPickerDialog(
      role = Role.Checkbox,
      nullable = true,
      visible = showStoresPickerDialog,
      title = stringResource(R.string.stores),
      selected = viewModel.filters.stores,
      items = allStores,
      itemKey = { it.id },
      itemText = { it.name },
      onChoose = { viewModel.onStoresChanged(it) },
      onDismiss = { showStoresPickerDialog = false }
    )

    FullScreenItemPickerDialog(
      role = Role.Checkbox,
      nullable = true,
      visible = showContributorsPickerDialog,
      title = stringResource(R.string.contributors),
      selected = viewModel.filters.contributors,
      items = allPersons,
      itemKey = { it.id },
      itemText = { it.name },
      onChoose = { viewModel.onContributorsChanged(it) },
      onDismiss = { showContributorsPickerDialog = false }
    )

    Scaffold(
      modifier = Modifier
        .nestedScroll(scrollBehavior.nestedScrollConnection)
        .windowInsetsPadding(
          WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
        ),
      topBar = {
        SearchTopAppBar(
          scrollBehavior = scrollBehavior,
          backgroundColor = navigationBarColor,
          searchText = viewModel.filters.query,
          placeholderText = stringResource(R.string.library_search_placeholder),
          shouldRequestFocus = viewModel.state != SearchState.RESULTS,
          onNavigationClick = { navigator.pop() },
          onClearClick = { viewModel.clearSearch() },
          onSearchTextChanged = { viewModel.onSearchTextChanged(it) },
          onSearchAction = { viewModel.search() },
          bottomContent = {
            Divider(color = LocalContentColor.current.copy(alpha = DividerOpacity))
          }
        )
      },
      content = { innerPadding ->
        Crossfade(
          modifier = Modifier.fillMaxSize(),
          targetState = viewModel.state
        ) { state ->
          when (state) {
            SearchState.HISTORY -> {
              NoItemsFound(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(innerPadding),
                icon = Icons.Outlined.History
              )
            }
            SearchState.NO_RESULTS_FOUND -> {
              NoItemsFound(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(innerPadding),
                text = stringResource(R.string.no_results_found),
                icon = Icons.Outlined.SearchOff
              )
            }
            SearchState.RESULTS -> {
              ResultsGrid(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                  start = 4.dp,
                  end = 4.dp,
                  top = innerPadding.calculateTopPadding() + 4.dp,
                  bottom = innerPadding.calculateBottomPadding() + 4.dp
                ),
                results = viewModel.results,
                onResultClick = {
                  navigator.push(BookScreen(it.id))
                }
              )
            }
          }
        }
      },
      bottomBar = {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = ModalBottomSheetLargeShape,
          tonalElevation = 6.dp
        ) {
          ChipsRow(
            modifier = Modifier
              .navigationBarsPadding()
              .imePadding(),
            shape = MaterialTheme.shapes.extraLarge,
            isFuture = viewModel.filters.isFuture,
            favoritesOnly = viewModel.filters.favoritesOnly,
            groupsCount = viewModel.filters.groups.size,
            showGroups = allGroups.isNotEmpty(),
            contributorsCount = viewModel.filters.contributors.size,
            showContributors = allPersons.isNotEmpty(),
            publishersCount = viewModel.filters.publishers.size,
            showPublishers = allPublishers.isNotEmpty(),
            storesCount = viewModel.filters.stores.size,
            showStores = allStores.isNotEmpty(),
            boughtAtSelected = viewModel.filters.boughtAt != null,
            readAtSelected = viewModel.filters.readAt != null,
            onIsFutureChanged = { viewModel.onIsFutureChanged(it) },
            onFavoritesOnlyChanged = { viewModel.onFavoritesOnlyChanged(it) },
            onGroupsClick = { showGroupsPickerDialog = true },
            onContributorsClick = { showContributorsPickerDialog = true },
            onPublishersClick = { showPublishersPickerDialog = true },
            onStoresClick = { showStoresPickerDialog = true },
            onBoughtAtClick = {
              showDateRangePicker(
                activity = activity,
                titleText = boughtAtPickerTitle,
                range = viewModel.filters.boughtAt,
                onRangeChoose = { viewModel.onBoughtAtChanged(it) }
              )
            },
            onReadAtClick = {
              showDateRangePicker(
                activity = activity,
                titleText = readAtPickerTitle,
                range = viewModel.filters.readAt,
                onRangeChoose = { viewModel.onReadAtChanged(it) }
              )
            }
          )
        }
      }
    )
  }

  @Composable
  fun ChipsRow(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
    isFuture: Boolean?,
    favoritesOnly: Boolean,
    groupsCount: Int,
    showGroups: Boolean = true,
    contributorsCount: Int,
    showContributors: Boolean = true,
    publishersCount: Int,
    showPublishers: Boolean = true,
    storesCount: Int,
    showStores: Boolean = true,
    boughtAtSelected: Boolean = false,
    readAtSelected: Boolean = false,
    onIsFutureChanged: (Boolean?) -> Unit,
    onFavoritesOnlyChanged: (Boolean) -> Unit,
    onGroupsClick: () -> Unit,
    onContributorsClick: () -> Unit,
    onPublishersClick: () -> Unit,
    onStoresClick: () -> Unit,
    onBoughtAtClick: () -> Unit,
    onReadAtClick: () -> Unit,
  ) {
    LazyRow(
      modifier = Modifier
        .fillMaxWidth()
        .then(modifier),
      state = rememberLazyListState(),
      contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      item("is_future") {
        FilterChip(
          shape = shape,
          selected = isFuture != null,
          onClick = {
            val newState = when (isFuture) {
              true -> false
              false -> null
              null -> true
            }
            onIsFutureChanged.invoke(newState)
          },
          label = { Text(stringResource(R.string.filter_future_only)) },
          leadingIcon = {
            Icon(
              imageVector = Icons.Outlined.Schedule,
              contentDescription = null,
              modifier = Modifier.size(FilterChipDefaults.IconSize)
            )
          },
          selectedIcon = {
            Icon(
              imageVector = if (isFuture == true) Icons.Filled.Done else Icons.Filled.Remove,
              contentDescription = stringResource(R.string.filter_future_only),
              modifier = Modifier.size(FilterChipDefaults.IconSize)
            )
          }
        )
      }
      item("favorites_only") {
        FilterChip(
          shape = shape,
          selected = favoritesOnly,
          onClick = { onFavoritesOnlyChanged.invoke(!favoritesOnly) },
          label = { Text(stringResource(R.string.filter_favorite)) },
          leadingIcon = {
            Icon(
              imageVector = Icons.Outlined.StarOutline,
              contentDescription = null,
              modifier = Modifier.size(FilterChipDefaults.IconSize)
            )
          },
          selectedIcon = {
            Icon(
              imageVector = Icons.Filled.Done,
              contentDescription = stringResource(R.string.filter_favorite),
              modifier = Modifier.size(FilterChipDefaults.IconSize)
            )
          }
        )
      }
      if (showContributors) {
        item("contributors") {
          FilterChip(
            shape = shape,
            selected = contributorsCount > 0,
            onClick = { onContributorsClick.invoke() },
            label = { Text(stringResource(R.string.contributors)) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Outlined.Group,
                contentDescription = null,
                modifier = Modifier.size(FilterChipDefaults.IconSize)
              )
            },
            selectedIcon = {
              Badge { Text(contributorsCount.toString()) }
            },
            trailingIcon = {
              Icon(
                imageVector = Icons.Outlined.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(FilterChipDefaults.IconSize)
              )
            }
          )
        }
      }
      if (showPublishers) {
        item("publishers") {
          FilterChip(
            shape = shape,
            selected = publishersCount > 0,
            onClick = { onPublishersClick.invoke() },
            label = { Text(stringResource(R.string.publishers)) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Outlined.Domain,
                contentDescription = null,
                modifier = Modifier.size(FilterChipDefaults.IconSize)
              )
            },
            selectedIcon = {
              Badge { Text(publishersCount.toString()) }
            },
            trailingIcon = {
              Icon(
                imageVector = Icons.Outlined.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(FilterChipDefaults.IconSize)
              )
            }
          )
        }
      }
      if (showGroups) {
        item("groups") {
          FilterChip(
            shape = shape,
            selected = groupsCount > 0,
            onClick = { onGroupsClick.invoke() },
            label = { Text(stringResource(R.string.groups)) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Outlined.GroupWork,
                contentDescription = null,
                modifier = Modifier.size(FilterChipDefaults.IconSize)
              )
            },
            selectedIcon = {
              Badge { Text(groupsCount.toString()) }
            },
            trailingIcon = {
              Icon(
                imageVector = Icons.Outlined.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(FilterChipDefaults.IconSize)
              )
            }
          )
        }
      }
      if (showStores) {
        item("stores") {
          FilterChip(
            shape = shape,
            selected = storesCount > 0,
            onClick = { onStoresClick.invoke() },
            label = { Text(stringResource(R.string.stores)) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Outlined.LocalMall,
                contentDescription = null,
                modifier = Modifier.size(FilterChipDefaults.IconSize)
              )
            },
            selectedIcon = {
              Badge { Text(storesCount.toString()) }
            },
            trailingIcon = {
              Icon(
                imageVector = Icons.Outlined.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(FilterChipDefaults.IconSize)
              )
            }
          )
        }
      }
      item("bought_at") {
        FilterChip(
          shape = shape,
          selected = boughtAtSelected,
          onClick = { onBoughtAtClick.invoke() },
          label = { Text(stringResource(R.string.filter_bought_at)) },
          leadingIcon = {
            Icon(
              imageVector = Icons.Outlined.DateRange,
              contentDescription = null,
              modifier = Modifier.size(FilterChipDefaults.IconSize)
            )
          },
          selectedIcon = {
            Icon(
              imageVector = Icons.Outlined.Done,
              contentDescription = null,
              modifier = Modifier.size(FilterChipDefaults.IconSize)
            )
          },
          trailingIcon = {
            Icon(
              imageVector = Icons.Outlined.ArrowDropDown,
              contentDescription = null,
              modifier = Modifier.size(FilterChipDefaults.IconSize)
            )
          }
        )
      }
      item("read_at") {
        FilterChip(
          shape = shape,
          selected = readAtSelected,
          onClick = { onReadAtClick.invoke() },
          label = { Text(stringResource(R.string.filter_read_at)) },
          leadingIcon = {
            Icon(
              imageVector = Icons.Outlined.DateRange,
              contentDescription = null,
              modifier = Modifier.size(FilterChipDefaults.IconSize)
            )
          },
          selectedIcon = {
            Icon(
              imageVector = Icons.Outlined.Done,
              contentDescription = null,
              modifier = Modifier.size(FilterChipDefaults.IconSize)
            )
          },
          trailingIcon = {
            Icon(
              imageVector = Icons.Outlined.ArrowDropDown,
              contentDescription = null,
              modifier = Modifier.size(FilterChipDefaults.IconSize)
            )
          }
        )
      }
    }
  }

  @Composable
  fun ResultsGrid(
    modifier: Modifier,
    results: SnapshotStateList<Book>,
    contentPadding: PaddingValues = PaddingValues(4.dp),
    columns: GridCells = GridCells.Adaptive(minSize = 96.dp),
    onResultClick: (Book) -> Unit
  ) {
    val gridState = rememberLazyGridState()

    LazyVerticalGrid(
      modifier = modifier,
      columns = columns,
      state = gridState,
      contentPadding = contentPadding,
      verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      items(results, key = { it.id }) { book ->
        BookCard(
          modifier = Modifier
            .fillMaxWidth()
            .animateItemPlacement(),
          title = book.title,
          coverUrl = book.cover_url,
          isFuture = book.is_future,
          onClick = { onResultClick.invoke(book) }
        )
      }
    }
  }

  private fun showDateRangePicker(
    activity: AppCompatActivity,
    titleText: String,
    range: DateRange? = null,
    onRangeChoose: (DateRange?) -> Unit
  ) {
    val constraints = CalendarConstraints.Builder()
      .setValidator(DateValidatorPointBackward.now())
      .build()

    val today = MaterialDatePicker.todayInUtcMilliseconds()

    val picker = MaterialDatePicker.Builder.dateRangePicker()
      .setTitleText(titleText)
      .setSelection(range?.toSelection() ?: Pair(today, today))
      .setCalendarConstraints(constraints)
      .setNegativeButtonText(R.string.action_clear)
      .build()

    picker.addOnPositiveButtonClickListener {
      onRangeChoose(DateRange.fromSelection(it))
    }
    picker.addOnNegativeButtonClickListener {
      onRangeChoose(null)
    }
    picker.show(activity.supportFragmentManager, picker.toString())
  }

}