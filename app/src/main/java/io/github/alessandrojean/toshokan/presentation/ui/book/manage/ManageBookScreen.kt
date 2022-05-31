package io.github.alessandrojean.toshokan.presentation.ui.book.manage

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Badge
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.Person
import io.github.alessandrojean.toshokan.domain.Contributor
import io.github.alessandrojean.toshokan.domain.CreditRole
import io.github.alessandrojean.toshokan.presentation.extensions.surfaceWithTonalElevation
import io.github.alessandrojean.toshokan.presentation.ui.book.BookScreen
import io.github.alessandrojean.toshokan.presentation.ui.book.manage.components.ContributorsTab
import io.github.alessandrojean.toshokan.presentation.ui.book.manage.components.CoverTab
import io.github.alessandrojean.toshokan.presentation.ui.book.manage.components.InformationTab
import io.github.alessandrojean.toshokan.presentation.ui.book.manage.components.OrganizationTab
import io.github.alessandrojean.toshokan.presentation.ui.library.LibraryScreen
import io.github.alessandrojean.toshokan.presentation.ui.theme.DividerOpacity
import io.github.alessandrojean.toshokan.presentation.ui.theme.ModalBottomSheetShape
import io.github.alessandrojean.toshokan.service.lookup.LookupBookResult
import kotlinx.coroutines.launch
import java.util.Locale

data class ManageBookScreen(val lookupBook: LookupBookResult? = null) : AndroidScreen() {
  
  @Composable
  override fun Content() {
    val manageBookViewModel = getViewModel<ManageBookViewModel>()
    val navigator = LocalNavigator.currentOrThrow
    val pagerState = rememberPagerState(initialPage = 0)
    val scope = rememberCoroutineScope()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior() }

    val tabs = listOf(
      ManageBookTab.Information,
      ManageBookTab.Contributors,
      ManageBookTab.Organization,
      ManageBookTab.Cover
    )

    val tabState = listOf(
      manageBookViewModel.informationTabInvalid,
      manageBookViewModel.contributorsTabInvalid,
      manageBookViewModel.organizationTabInvalid
    )

    val allPublishers by manageBookViewModel.publishers.collectAsState(emptyList())
    val allStores by manageBookViewModel.stores.collectAsState(emptyList())
    val allGroups by manageBookViewModel.groups.collectAsState(emptyList())

    LaunchedEffect(lookupBook) {
      if (lookupBook != null) {
        scope.launch { manageBookViewModel.setFieldValues(lookupBook) }
      }
    }

    val systemUiController = rememberSystemUiController()
    val statusBarColor = when {
      scrollBehavior.scrollFraction > 0 -> TopAppBarDefaults
        .smallTopAppBarColors()
        .containerColor(scrollBehavior.scrollFraction)
        .value
      else -> MaterialTheme.colorScheme.surface
    }

    SideEffect {
      systemUiController.setStatusBarColor(
        color = statusBarColor
      )
    }

    LaunchedEffect(pagerState.currentPage) {
      if (tabs[pagerState.currentPage] is ManageBookTab.Cover) {
        scope.launch { manageBookViewModel.fetchCovers() }
      }
    }

    val modalBottomSheetState = rememberModalBottomSheetState(
      initialValue = ModalBottomSheetValue.Hidden,
      skipHalfExpanded = true
    )

    var showContributorPickerDialog by remember { mutableStateOf(false) }
    val allPeople by manageBookViewModel.people.collectAsState(initial = emptyList())

    if (showContributorPickerDialog) {
      ContributorPickerDialog(
        allPeople = allPeople,
        contributor = manageBookViewModel.selectedContributor,
        onDismiss = {
          showContributorPickerDialog = false
          manageBookViewModel.selectedContributor = null
        },
        onFinish = { contributor ->
          manageBookViewModel.handleContributor(contributor)
          showContributorPickerDialog = false
        }
      )
    }

    // Disable the back handling when writing to the database.
    BackHandler(manageBookViewModel.writing) {}

    ModalBottomSheetLayout(
      sheetState = modalBottomSheetState,
      sheetShape = ModalBottomSheetShape,
      sheetBackgroundColor = Color.Transparent,
      sheetContent = {
        ModalBottomSheet(
          contributor = manageBookViewModel.selectedContributor,
          onEditClick = {
            scope.launch { modalBottomSheetState.hide() }
            showContributorPickerDialog = true
          },
          onRemoveClick = {
            scope.launch { modalBottomSheetState.hide() }
            manageBookViewModel.removeSelectedContributor()
          }
        )
      }
    ) {
      Scaffold(
        modifier = Modifier
          .nestedScroll(scrollBehavior.nestedScrollConnection)
          .statusBarsPadding(),
        topBar = {
          SmallTopAppBar(
            scrollBehavior = scrollBehavior,
            navigationIcon = {
              IconButton(
                enabled = !manageBookViewModel.writing,
                onClick = { navigator.pop() }
              ) {
                Icon(
                  imageVector = Icons.Outlined.ArrowBack,
                  contentDescription = stringResource(R.string.action_back)
                )
              }
            },
            title = { Text(stringResource(R.string.create_book)) },
            actions = {
              TextButton(
                enabled = !manageBookViewModel.informationTabInvalid &&
                  !manageBookViewModel.contributorsTabInvalid &&
                  !manageBookViewModel.informationTabInvalid &&
                  !manageBookViewModel.writing,
                onClick = {
                  manageBookViewModel.create { bookId ->
                    if (bookId != null) {
                      navigator.popUntil { it is LibraryScreen }
                      navigator.push(BookScreen(bookId))
                    }
                  }
                }
              ) {
                Text(stringResource(R.string.action_finish))
              }
            }
          )
        },
        content = { innerPadding ->
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          ) {
            ScrollableTabRow(
              selectedTabIndex = pagerState.currentPage,
              edgePadding = 12.dp,
              containerColor = statusBarColor,
              indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                  Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage])
                )
              }
            ) {
              tabs.forEachIndexed { index, tab ->
                Tab(
                  text = {
                    Row(
                      horizontalArrangement = Arrangement.Center,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(stringResource(tab.title))

                      if (tabState.getOrElse(index) { false }) {
                        Badge(modifier = Modifier.padding(start = 8.dp))
                      }
                    }
                  },
                  selected = pagerState.currentPage == index,
                  selectedContentColor = MaterialTheme.colorScheme.primary,
                  unselectedContentColor = MaterialTheme.colorScheme.onBackground,
                  onClick = {
                    scope.launch { pagerState.animateScrollToPage(index) }
                  }
                )
              }
            }
            HorizontalPager(
              state = pagerState,
              count = tabs.size,
              verticalAlignment = Alignment.Top,
            ) { page ->
              when (tabs[page]) {
                is ManageBookTab.Information -> {
                  InformationTab(
                    code = manageBookViewModel.code,
                    title = manageBookViewModel.title,
                    synopsis = manageBookViewModel.synopsis,
                    publisher = manageBookViewModel.publisher,
                    publisherText = manageBookViewModel.publisherText,
                    allPublishers = allPublishers,
                    labelPriceCurrency = manageBookViewModel.labelPriceCurrency,
                    labelPriceValue = manageBookViewModel.labelPriceValue,
                    paidPriceCurrency = manageBookViewModel.paidPriceCurrency,
                    paidPriceValue = manageBookViewModel.paidPriceValue,
                    dimensionWidth = manageBookViewModel.dimensionWidth,
                    dimensionHeight = manageBookViewModel.dimensionHeight,
                    onCodeChange = { manageBookViewModel.code = it },
                    onTitleChange = { manageBookViewModel.title = it },
                    onSynopsisChange = { manageBookViewModel.synopsis = it },
                    onPublisherTextChange = { manageBookViewModel.publisherText = it },
                    onPublisherChange = { manageBookViewModel.publisher = it },
                    onLabelPriceValueChange = { manageBookViewModel.labelPriceValue = it },
                    onLabelPriceCurrencyChange = { manageBookViewModel.labelPriceCurrency = it },
                    onPaidPriceValueChange = { manageBookViewModel.paidPriceValue = it },
                    onPaidPriceCurrencyChange = { manageBookViewModel.paidPriceCurrency = it },
                    onDimensionWidthChange = { manageBookViewModel.dimensionWidth = it },
                    onDimensionHeightChange = { manageBookViewModel.dimensionHeight = it }
                  )
                }
                is ManageBookTab.Contributors -> {
                  ContributorsTab(
                    writing = manageBookViewModel.writing,
                    contributors = manageBookViewModel.contributors,
                    onAddContributorClick = { showContributorPickerDialog = true },
                    onContributorLongClick = { contributor ->
                      manageBookViewModel.selectedContributor = contributor
                      scope.launch { modalBottomSheetState.show() }
                    }
                  )
                }
                is ManageBookTab.Organization -> {
                  OrganizationTab(
                    store = manageBookViewModel.store,
                    storeText = manageBookViewModel.storeText,
                    allStores = allStores,
                    boughtAt = manageBookViewModel.boughtAt,
                    group = manageBookViewModel.group,
                    groupText = manageBookViewModel.groupText,
                    allGroups = allGroups,
                    notes = manageBookViewModel.notes,
                    isFuture = manageBookViewModel.isFuture,
                    onStoreTextChange = { manageBookViewModel.storeText = it },
                    onStoreChange = { manageBookViewModel.store = it },
                    onBoughtAtChange = { manageBookViewModel.boughtAt = it },
                    onGroupTextChange = { manageBookViewModel.groupText = it },
                    onGroupChange = { manageBookViewModel.group = it },
                    onNotesChange = { manageBookViewModel.notes = it },
                    onIsFutureChange = { manageBookViewModel.isFuture = it }
                  )
                }
                is ManageBookTab.Cover -> {
                  CoverTab(
                    coverUrl = manageBookViewModel.coverUrl,
                    allCovers = manageBookViewModel.allCovers,
                    state = manageBookViewModel.coverState,
                    canRefresh = manageBookViewModel.coverRefreshEnabled(),
                    onChange = { manageBookViewModel.coverUrl = it.imageUrl },
                    onRefresh = { manageBookViewModel.fetchCovers() }
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
  fun ModalBottomSheet(
    contributor: Contributor?,
    onEditClick: () -> Unit,
    onRemoveClick: () -> Unit
  ) {
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
      ) {
        Text(
          modifier = Modifier.padding(top = 24.dp, start = 24.dp, end = 24.dp),
          text = contributor?.personText.orEmpty(),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          style = MaterialTheme.typography.titleLarge
        )
        Text(
          modifier = Modifier.padding(horizontal = 24.dp),
          text = stringResource(
            R.string.person_role,
            stringResource(contributor?.role?.title ?: CreditRole.UNKNOWN.title)
              .lowercase(Locale.getDefault())
          ),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontStyle = FontStyle.Italic
          )
        )
        Divider(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
          color = LocalContentColor.current.copy(alpha = DividerOpacity)
        )
        ModalBottomSheetItem(
          modifier = Modifier.fillMaxWidth(),
          text = stringResource(R.string.action_edit_contributor),
          icon = Icons.Outlined.Edit,
          onClick = onEditClick
        )
        ModalBottomSheetItem(
          modifier = Modifier.fillMaxWidth(),
          text = stringResource(R.string.action_remove_contributor),
          icon = Icons.Outlined.Delete,
          onClick = onRemoveClick
        )
        Spacer(modifier = Modifier
          .fillMaxWidth()
          .height(8.dp))
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
        .then(modifier),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = text
      )
      Text(
        text = text,
        modifier = Modifier.padding(start = 24.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }

  @Composable
  private fun ContributorPickerDialog(
    allPeople: List<Person>,
    contributor: Contributor? = null,
    onDismiss: () -> Unit,
    onFinish: (Contributor) -> Unit
  ) {
    var selectedPersonText by remember { mutableStateOf(contributor?.personText.orEmpty()) }
    var selectedPerson by remember { mutableStateOf(contributor?.person) }
    var selectedRole by remember { mutableStateOf(contributor?.role ?: CreditRole.UNKNOWN) }

    val pagerState = rememberPagerState(initialPage = 0)
    val scope = rememberCoroutineScope()

    Dialog(
      onDismissRequest = onDismiss,
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      Scaffold(
        topBar = {
          Column(modifier = Modifier.fillMaxWidth()) {
            SmallTopAppBar(
              navigationIcon = {
                IconButton(onClick = onDismiss) {
                  Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.action_cancel)
                  )
                }
              },
              title = {
                Text(
                  text = if (contributor == null) {
                    stringResource(R.string.new_contributor)
                  } else {
                    stringResource(R.string.editing_contributor)
                  }
                )
              },
            )
            Divider(
              modifier = Modifier.fillMaxWidth(),
              color = LocalContentColor.current.copy(alpha = DividerOpacity)
            )
          }
        },
        content = { innerPadding ->
          HorizontalPager(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding),
            state = pagerState,
            count = 2,
            userScrollEnabled = false,
            verticalAlignment = Alignment.Top
          ) {
            when (currentPage) {
              STEP_PICK_PERSON -> {
                StepPickPerson(
                  modifier = Modifier.fillMaxSize(),
                  person = selectedPerson,
                  personText = selectedPersonText,
                  onPersonTextChange = { selectedPersonText = it },
                  onPersonChange = {
                    selectedPerson = it

                    if (it != null) {
                      scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                      }
                    }
                  },
                  allPeople = allPeople
                )
              }
              STEP_PICK_ROLE -> {
                StepPickRole(
                  modifier = Modifier.fillMaxSize(),
                  role = selectedRole,
                  onRoleChange = {
                    selectedRole = it

                    onFinish(
                      Contributor(
                        person = selectedPerson,
                        personText = selectedPersonText,
                        role = selectedRole
                      )
                    )
                  }
                )
              }
            }
          }
        },
        bottomBar = {
          Column(modifier = Modifier.fillMaxWidth()) {
            Divider(
              modifier = Modifier.fillMaxWidth(),
              color = LocalContentColor.current.copy(alpha = DividerOpacity)
            )
            BottomAppBar(
              contentPadding = PaddingValues(horizontal = 16.dp),
              containerColor = MaterialTheme.colorScheme.surface,
              tonalElevation = 0.dp
            ) {
              Text(
                text = stringResource(
                  R.string.steps,
                  pagerState.currentPage + 1,
                  pagerState.pageCount
                )
              )
              Spacer(modifier = Modifier.weight(1f))
              IconButton(
                onClick = {
                  scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                  }
                },
                enabled = pagerState.currentPage > 0
              ) {
                Icon(
                  imageVector = Icons.Outlined.ChevronLeft,
                  contentDescription = stringResource(R.string.action_previous_step)
                )
              }
              IconButton(
                onClick = {
                  scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                  }
                },
                enabled = pagerState.currentPage < pagerState.pageCount - 1 &&
                  selectedPersonText.isNotBlank()
              ) {
                Icon(
                  imageVector = Icons.Outlined.ChevronRight,
                  contentDescription = stringResource(R.string.action_next_step)
                )
              }
            }
          }
        }
      )
    }
  }

  @Composable
  private fun StepPickPerson(
    modifier: Modifier = Modifier,
    person: Person?,
    personText: String,
    onPersonTextChange: (String) -> Unit,
    onPersonChange: (Person?) -> Unit,
    allPeople: List<Person>
  ) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    val filteredPeople by remember(personText) {
      derivedStateOf {
        allPeople.filter { it.name.contains(personText, ignoreCase = true) }
      }
    }

    Column(
      modifier = modifier,
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 16.dp, end = 16.dp, top = 8.dp),
        value = personText,
        onValueChange = onPersonTextChange,
        singleLine = true,
        label = { Text(stringResource(R.string.person)) },
        leadingIcon = {
          Icon(
            imageVector = Icons.Outlined.PersonAdd,
            contentDescription = null
          )
        },
        trailingIcon = {
          IconButton(
            onClick = {
              onPersonChange(null)
              onPersonTextChange("")
            }
          ) {
            Icon(
              imageVector = Icons.Outlined.Clear,
              contentDescription = stringResource(R.string.action_clear)
            )
          }
        },
        isError = personText.isBlank(),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
          onDone = { keyboardController?.hide() }
        )
      )
      LazyColumn(
        state = listState,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      ) {
        items(filteredPeople, key = { it.id }) { personOption ->
          PickContributorOption(
            modifier = Modifier
              .fillMaxWidth()
              .animateItemPlacement(),
            text = personOption.name,
            selected = personOption.id == person?.id,
            onClick = {
              onPersonTextChange(personOption.name)
              onPersonChange(personOption)
            }
          )
        }
      }
    }
  }

  @Composable
  private fun StepPickRole(
    modifier: Modifier = Modifier,
    role: CreditRole,
    onRoleChange: (CreditRole) -> Unit
  ) {
    val listState = rememberLazyListState()
    val allRoles = remember { CreditRole.values() }

    LazyColumn(
      state = listState,
      modifier = modifier
    ) {
      items(allRoles, key = { it.code }) { roleOption ->
        PickContributorOption(
          modifier = Modifier.fillMaxWidth(),
          text = stringResource(roleOption.title),
          selected = roleOption == role,
          onClick = { onRoleChange(roleOption) }
        )
      }
    }
  }

  @Composable
  private fun PickContributorOption(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
  ) {
    Row(
      modifier = Modifier
        .selectable(
          selected = selected,
          onClick = onClick,
          role = Role.RadioButton
        )
        .padding(16.dp)
        .then(modifier),
      verticalAlignment = Alignment.CenterVertically
    ) {
      RadioButton(
        selected = selected,
        onClick = null
      )
      Text(
        text = text,
        modifier = Modifier.padding(start = 24.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }

  sealed class ManageBookTab(@StringRes val title: Int) {
    object Information : ManageBookTab(R.string.information)
    object Contributors : ManageBookTab(R.string.contributors)
    object Organization : ManageBookTab(R.string.organization)
    object Cover : ManageBookTab(R.string.cover)
  }

  companion object {
    private const val STEP_PICK_PERSON = 0
    private const val STEP_PICK_ROLE = 1
  }
  
}