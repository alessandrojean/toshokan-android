package io.github.alessandrojean.toshokan.presentation.ui.search

import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explicit
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.domain.SearchFilters
import io.github.alessandrojean.toshokan.presentation.extensions.surfaceWithTonalElevation
import io.github.alessandrojean.toshokan.presentation.ui.book.BookScreen
import io.github.alessandrojean.toshokan.presentation.ui.book.BookScreen.BookData
import io.github.alessandrojean.toshokan.presentation.ui.core.components.BoxedCircularProgressIndicator
import io.github.alessandrojean.toshokan.presentation.ui.core.components.ModalBottomSheet
import io.github.alessandrojean.toshokan.presentation.ui.core.components.NoItemsFound
import io.github.alessandrojean.toshokan.presentation.ui.core.components.SearchTopAppBar
import io.github.alessandrojean.toshokan.presentation.ui.core.dialog.FullScreenItemPickerDialog
import io.github.alessandrojean.toshokan.presentation.ui.core.picker.showDateRangePicker
import io.github.alessandrojean.toshokan.presentation.ui.search.components.SearchFilterChipsRow
import io.github.alessandrojean.toshokan.presentation.ui.search.components.SearchResultsGrid
import io.github.alessandrojean.toshokan.presentation.ui.search.components.SearchSuggestions
import io.github.alessandrojean.toshokan.presentation.ui.search.components.SortModalBottomSheetContent
import io.github.alessandrojean.toshokan.presentation.ui.theme.DividerOpacity
import io.github.alessandrojean.toshokan.util.extension.collectAsStateWithLifecycle
import io.github.alessandrojean.toshokan.util.extension.navigationBarsWithIme
import io.github.alessandrojean.toshokan.util.extension.plus
import kotlinx.coroutines.launch

class SearchScreen(private val filters: SearchFilters? = null) : AndroidScreen() {

  @Composable
  override fun Content() {
    val screenModel = getScreenModel<SearchScreenModel, SearchScreenModel.Factory> { factory ->
      factory.create(filters)
    }
    val navigator = LocalNavigator.currentOrThrow
    val activity = LocalContext.current as? AppCompatActivity
    val topAppBarScrollState = rememberTopAppBarScrollState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior(topAppBarScrollState) }
    val state by screenModel.state.collectAsStateWithLifecycle()

    val navigationBarColor = MaterialTheme.colorScheme.surfaceWithTonalElevation(6.dp)

    val allGroups by screenModel.allGroups.collectAsStateWithLifecycle(emptyList())
    val allPersons by screenModel.allPersons.collectAsStateWithLifecycle(emptyList())
    val allPublishers by screenModel.allPublishers.collectAsStateWithLifecycle(emptyList())
    val allStores by screenModel.allStores.collectAsStateWithLifecycle(emptyList())
    val allCollections by screenModel.allCollections.collectAsStateWithLifecycle(emptyList())
    val allTags by screenModel.allTags.collectAsStateWithLifecycle(emptyList())

    val collections by remember(allCollections) {
      derivedStateOf { allCollections.filter { it.count > 1 } }
    }

    var showGroupsPickerDialog by remember { mutableStateOf(false) }
    var showContributorsPickerDialog by remember { mutableStateOf(false) }
    var showPublishersPickerDialog by remember { mutableStateOf(false) }
    var showStoresPickerDialog by remember { mutableStateOf(false) }
    var showCollectionsPickerDialog by remember { mutableStateOf(false) }
    var showTagsPickerDialog by remember { mutableStateOf(false) }

    val boughtAtPickerTitle = stringResource(R.string.filter_bought_at)
    val readAtPickerTitle = stringResource(R.string.filter_read_at)

    FullScreenItemPickerDialog(
      role = Role.Checkbox,
      nullable = true,
      visible = showGroupsPickerDialog,
      title = stringResource(R.string.groups),
      selected = screenModel.filters.groups,
      items = allGroups,
      itemKey = { it.id },
      itemText = { it.name },
      onChoose = { screenModel.onGroupsChanged(it) },
      onDismiss = { showGroupsPickerDialog = false }
    )

    FullScreenItemPickerDialog(
      role = Role.Checkbox,
      nullable = true,
      visible = showTagsPickerDialog,
      title = stringResource(R.string.tags),
      selected = screenModel.filters.tags,
      items = allTags,
      itemKey = { it.id },
      itemText = { it.name },
      itemTrailingIcon = {
        if (it.is_nsfw) {
          Icon(
            painter = rememberVectorPainter(Icons.Outlined.Explicit),
            contentDescription = null
          )
        }
      },
      onChoose = { screenModel.onTagsChanged(it) },
      onDismiss = { showTagsPickerDialog = false }
    )

    FullScreenItemPickerDialog(
      role = Role.Checkbox,
      nullable = true,
      visible = showPublishersPickerDialog,
      title = stringResource(R.string.publishers),
      selected = screenModel.filters.publishers,
      items = allPublishers,
      itemKey = { it.id },
      itemText = { it.name },
      onChoose = { screenModel.onPublishersChanged(it) },
      onDismiss = { showPublishersPickerDialog = false }
    )

    FullScreenItemPickerDialog(
      role = Role.Checkbox,
      nullable = true,
      visible = showStoresPickerDialog,
      title = stringResource(R.string.stores),
      selected = screenModel.filters.stores,
      items = allStores,
      itemKey = { it.id },
      itemText = { it.name },
      onChoose = { screenModel.onStoresChanged(it) },
      onDismiss = { showStoresPickerDialog = false }
    )

    FullScreenItemPickerDialog(
      role = Role.Checkbox,
      nullable = true,
      visible = showContributorsPickerDialog,
      title = stringResource(R.string.contributors),
      selected = screenModel.filters.contributors,
      items = allPersons,
      itemKey = { it.id },
      itemText = { it.name },
      onChoose = { screenModel.onContributorsChanged(it) },
      onDismiss = { showContributorsPickerDialog = false }
    )

    FullScreenItemPickerDialog(
      role = Role.Checkbox,
      nullable = true,
      visible = showCollectionsPickerDialog,
      title = stringResource(R.string.filter_collection),
      selected = screenModel.filters.collections,
      items = collections,
      itemKey = { it.hashCode() },
      itemText = { it.title },
      itemTrailingIcon = { collection ->
        collection.groupName?.let { groupName ->
          Text(
            text = groupName,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      },
      onChoose = { screenModel.onCollectionsChanged(it) },
      onDismiss = { showCollectionsPickerDialog = false }
    )

    val scope = rememberCoroutineScope()
    val modalBottomSheetState = rememberModalBottomSheetState(
      initialValue = ModalBottomSheetValue.Hidden,
      skipHalfExpanded = true
    )
    val focusManager = LocalFocusManager.current

    BackHandler(modalBottomSheetState.isVisible) {
      scope.launch { modalBottomSheetState.hide() }
    }

    val gridState = rememberLazyGridState()
    val fabExpanded by remember {
      derivedStateOf {
        gridState.firstVisibleItemIndex == 0 && gridState.firstVisibleItemScrollOffset == 0
      }
    }

    val bottomPadding = WindowInsets.navigationBarsWithIme.asPaddingValues()
    val fabHeightPadding = PaddingValues(bottom = 76.dp)

    ModalBottomSheetLayout(
      sheetBackgroundColor = Color.Transparent,
      sheetState = modalBottomSheetState,
      sheetContent = {
        ModalBottomSheet(
          modifier = Modifier.fillMaxWidth(),
          footer = {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              TextButton(onClick = { screenModel.clearSearch() }) {
                Text(stringResource(R.string.action_reset))
              }
              Button(
                onClick = {
                  scope.launch { modalBottomSheetState.hide() }
                }
              ) {
                Text(stringResource(R.string.action_close))
              }
            }
          },
          header = {
            SearchFilterChipsRow(
              shape = MaterialTheme.shapes.extraLarge,
              isFuture = screenModel.filters.isFuture,
              favoritesOnly = screenModel.filters.favoritesOnly,
              collectionsSelected = screenModel.filters.collections.isNotEmpty(),
              showCollections = collections.isNotEmpty(),
              groupsSelected = screenModel.filters.groups.isNotEmpty(),
              showGroups = allGroups.isNotEmpty(),
              tagsSelected = screenModel.filters.tags.isNotEmpty(),
              showTags = allTags.isNotEmpty(),
              contributorsSelected = screenModel.filters.contributors.isNotEmpty(),
              showContributors = allPersons.isNotEmpty(),
              publishersSelected = screenModel.filters.publishers.isNotEmpty(),
              showPublishers = allPublishers.isNotEmpty(),
              storesSelected = screenModel.filters.stores.isNotEmpty(),
              showStores = allStores.isNotEmpty(),
              boughtAtSelected = screenModel.filters.boughtAt != null,
              readAtSelected = screenModel.filters.readAt != null,
              onIsFutureChanged = { screenModel.onIsFutureChanged(it) },
              onFavoritesOnlyChanged = { screenModel.onFavoritesOnlyChanged(it) },
              onCollectionsClick = { showCollectionsPickerDialog = true },
              onGroupsClick = { showGroupsPickerDialog = true },
              onTagsClick = { showTagsPickerDialog = true },
              onContributorsClick = { showContributorsPickerDialog = true },
              onPublishersClick = { showPublishersPickerDialog = true },
              onStoresClick = { showStoresPickerDialog = true },
              onBoughtAtClick = {
                activity?.let {
                  showDateRangePicker(
                    activity = it,
                    titleText = boughtAtPickerTitle,
                    range = screenModel.filters.boughtAt,
                    onRangeChoose = { range -> screenModel.onBoughtAtChanged(range) }
                  )
                }
              },
              onReadAtClick = {
                activity?.let {
                  showDateRangePicker(
                    activity = it,
                    titleText = readAtPickerTitle,
                    range = screenModel.filters.readAt,
                    onRangeChoose = { range -> screenModel.onReadAtChanged(range) }
                  )
                }
              }
            )
          }
        ) {
          SortModalBottomSheetContent(
            column = screenModel.filters.sortColumn,
            direction = screenModel.filters.sortDirection,
            onColumnChange = { screenModel.onSortColumnChanged(it) },
            onDirectionChange = { screenModel.onSortDirectionChanged(it) }
          )
        }
      }
    ) {
      Scaffold(
        modifier = Modifier
          .fillMaxSize()
          .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
          SearchTopAppBar(
            scrollBehavior = scrollBehavior,
            containerColor = navigationBarColor,
            searchText = screenModel.query,
            placeholderText = stringResource(R.string.library_search_placeholder),
            shouldRequestFocus = state !is SearchScreenModel.State.Results && filters == null,
            onNavigationClick = { navigator.pop() },
            onClearClick = {
              screenModel.clearSearch()
            },
            onSearchTextChanged = { screenModel.onSearchTextChanged(it) },
            onSearchAction = { screenModel.search() },
            bottomContent = {
              Divider(color = LocalContentColor.current.copy(alpha = DividerOpacity))
            }
          )
        },
        floatingActionButton = {
          ExtendedFloatingActionButton(
            modifier = Modifier.padding(bottomPadding),
            expanded = fabExpanded,
            text = { Text(stringResource(R.string.action_filter)) },
            icon = {
              Icon(
                painter = rememberVectorPainter(Icons.Outlined.FilterList),
                contentDescription = stringResource(R.string.action_filter)
              )
            },
            onClick = {
              focusManager.clearFocus()
              scope.launch { modalBottomSheetState.show() }
            }
          )
        },
        content = { innerPadding ->
          val padding = innerPadding + bottomPadding

          Crossfade(
            modifier = Modifier.fillMaxSize(),
            targetState = state
          ) { state ->
            when (state) {
              SearchScreenModel.State.Empty -> {
                NoItemsFound(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                  icon = Icons.Outlined.Search
                )
              }
              SearchScreenModel.State.Loading -> {
                BoxedCircularProgressIndicator(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                )
              }
              SearchScreenModel.State.Suggestions -> {
                SearchSuggestions(
                  modifier = Modifier.fillMaxSize(),
                  contentPadding = padding + fabHeightPadding,
                  query = screenModel.filters.query.trim(),
                  suggestions = remember(allCollections) {
                    allCollections.map { it.title }.distinct()
                  },
                  onSuggestionClick = {
                    focusManager.clearFocus()
                    screenModel.onSearchTextChanged(TextFieldValue(it, TextRange(it.length)))
                    screenModel.search()
                  },
                  onSuggestionSelectClick = {
                    screenModel.onSearchTextChanged(TextFieldValue(it, TextRange(it.length)))
                  }
                )
              }
              SearchScreenModel.State.NoResultsFound -> {
                NoItemsFound(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                  text = stringResource(R.string.no_results_found),
                  icon = Icons.Outlined.SearchOff
                )
              }
              is SearchScreenModel.State.Results -> {
                SearchResultsGrid(
                  modifier = Modifier.fillMaxSize(),
                  contentPadding = padding + fabHeightPadding + PaddingValues(all = 4.dp),
                  state = gridState,
                  results = state.results,
                  onResultClick = {
                    navigator.push(BookScreen(BookData.Database(it.id)))
                  }
                )
              }
            }
          }
        }
      )
    }
  }

}