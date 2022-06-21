package io.github.alessandrojean.toshokan.presentation.ui.search

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Divider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.domain.SearchFilters
import io.github.alessandrojean.toshokan.presentation.extensions.surfaceWithTonalElevation
import io.github.alessandrojean.toshokan.presentation.ui.book.BookScreen
import io.github.alessandrojean.toshokan.presentation.ui.core.components.NoItemsFound
import io.github.alessandrojean.toshokan.presentation.ui.core.components.SearchTopAppBar
import io.github.alessandrojean.toshokan.presentation.ui.core.dialog.FullScreenItemPickerDialog
import io.github.alessandrojean.toshokan.presentation.ui.core.picker.showDateRangePicker
import io.github.alessandrojean.toshokan.presentation.ui.search.components.SearchFilterChipsRow
import io.github.alessandrojean.toshokan.presentation.ui.search.components.SearchResultsGrid
import io.github.alessandrojean.toshokan.presentation.ui.theme.DividerOpacity
import io.github.alessandrojean.toshokan.presentation.ui.theme.ModalBottomSheetLargeShape
import io.github.alessandrojean.toshokan.util.extension.collectAsStateWithLifecycle

class SearchScreen(private val filters: SearchFilters? = null) : AndroidScreen() {

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

        filters?.let {
          viewModel.onFiltersChanged(filters)
        }
      }
    )

    val allGroups by viewModel.allGroups.collectAsStateWithLifecycle(emptyList())
    val allPersons by viewModel.allPersons.collectAsStateWithLifecycle(emptyList())
    val allPublishers by viewModel.allPublishers.collectAsStateWithLifecycle(emptyList())
    val allStores by viewModel.allStores.collectAsStateWithLifecycle(emptyList())
    val allCollections by viewModel.allCollections.collectAsStateWithLifecycle(emptyList())

    var showGroupsPickerDialog by remember { mutableStateOf(false) }
    var showContributorsPickerDialog by remember { mutableStateOf(false) }
    var showPublishersPickerDialog by remember { mutableStateOf(false) }
    var showStoresPickerDialog by remember { mutableStateOf(false) }
    var showCollectionsPickerDialog by remember { mutableStateOf(false) }

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

    FullScreenItemPickerDialog(
      role = Role.Checkbox,
      nullable = true,
      visible = showCollectionsPickerDialog,
      title = stringResource(R.string.filter_collection),
      selected = viewModel.filters.collections,
      items = allCollections,
      itemKey = { it },
      itemText = { it },
      onChoose = { viewModel.onCollectionsChanged(it) },
      onDismiss = { showCollectionsPickerDialog = false }
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
          shouldRequestFocus = viewModel.state != SearchState.RESULTS && filters == null,
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
              SearchResultsGrid(
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
          SearchFilterChipsRow(
            modifier = Modifier
              .navigationBarsPadding()
              .imePadding(),
            shape = MaterialTheme.shapes.extraLarge,
            isFuture = viewModel.filters.isFuture,
            favoritesOnly = viewModel.filters.favoritesOnly,
            collectionsSelected = viewModel.filters.collections.isNotEmpty(),
            showCollections = allCollections.isNotEmpty(),
            groupsSelected = viewModel.filters.groups.isNotEmpty(),
            showGroups = allGroups.isNotEmpty(),
            contributorsSelected = viewModel.filters.contributors.isNotEmpty(),
            showContributors = allPersons.isNotEmpty(),
            publishersSelected = viewModel.filters.publishers.isNotEmpty(),
            showPublishers = allPublishers.isNotEmpty(),
            storesSelected = viewModel.filters.stores.isNotEmpty(),
            showStores = allStores.isNotEmpty(),
            boughtAtSelected = viewModel.filters.boughtAt != null,
            readAtSelected = viewModel.filters.readAt != null,
            onIsFutureChanged = { viewModel.onIsFutureChanged(it) },
            onFavoritesOnlyChanged = { viewModel.onFavoritesOnlyChanged(it) },
            onCollectionsClick = { showCollectionsPickerDialog = true },
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

}