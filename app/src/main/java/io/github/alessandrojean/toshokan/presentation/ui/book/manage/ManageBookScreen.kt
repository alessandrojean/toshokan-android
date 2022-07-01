package io.github.alessandrojean.toshokan.presentation.ui.book.manage

import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.Person
import io.github.alessandrojean.toshokan.domain.Contributor
import io.github.alessandrojean.toshokan.domain.CreditRole
import io.github.alessandrojean.toshokan.presentation.ui.book.BookScreen
import io.github.alessandrojean.toshokan.presentation.ui.book.manage.components.ContributorsTab
import io.github.alessandrojean.toshokan.presentation.ui.book.manage.components.CoverTab
import io.github.alessandrojean.toshokan.presentation.ui.book.manage.components.InformationTab
import io.github.alessandrojean.toshokan.presentation.ui.book.manage.components.OrganizationTab
import io.github.alessandrojean.toshokan.presentation.ui.book.manage.components.TagsTab
import io.github.alessandrojean.toshokan.presentation.ui.core.components.EnhancedSmallTopAppBar
import io.github.alessandrojean.toshokan.presentation.ui.core.components.ModalBottomSheet
import io.github.alessandrojean.toshokan.presentation.ui.core.components.ModalBottomSheetItem
import io.github.alessandrojean.toshokan.presentation.ui.core.components.ModalBottomSheetTitle
import io.github.alessandrojean.toshokan.presentation.ui.library.LibraryScreen
import io.github.alessandrojean.toshokan.presentation.ui.theme.DividerOpacity
import io.github.alessandrojean.toshokan.presentation.ui.theme.ModalBottomSheetExtraLargeShape
import io.github.alessandrojean.toshokan.service.lookup.LookupBookResult
import io.github.alessandrojean.toshokan.util.ConnectionState
import io.github.alessandrojean.toshokan.util.connectivityState
import io.github.alessandrojean.toshokan.util.extension.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.util.Locale

data class ManageBookScreen(
  val lookupBook: LookupBookResult? = null,
  val existingBookId: Long? = null,
  val initialTab: ManageBookTab = ManageBookTab.Information
) : AndroidScreen() {

  @Composable
  override fun Content() {
    val tabs = remember {
      listOf(
        ManageBookTab.Information,
        ManageBookTab.Contributors,
        ManageBookTab.Organization,
        ManageBookTab.Cover,
        ManageBookTab.Tags
      )
    }

    val manageBookScreenModel = getScreenModel<ManageBookScreenModel, ManageBookScreenModel.Factory> {
      it.create(lookupBook, existingBookId)
    }
    val navigator = LocalNavigator.currentOrThrow
    val pagerState = rememberPagerState(
      initialPage = tabs
        .indexOfFirst { it == initialTab }
        .coerceAtLeast(0)
    )
    val scope = rememberCoroutineScope()
    val topAppBarScrollState = rememberTopAppBarScrollState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior(topAppBarScrollState) }
    val internetConnection by connectivityState()

    val tabState = listOf(
      manageBookScreenModel.informationTabInvalid,
      manageBookScreenModel.contributorsTabInvalid,
      manageBookScreenModel.organizationTabInvalid
    )

    val allPublishers by manageBookScreenModel.publishers
      .collectAsStateWithLifecycle(emptyList())
    val allStores by manageBookScreenModel.stores
      .collectAsStateWithLifecycle(emptyList())
    val allGroups by manageBookScreenModel.groups
      .collectAsStateWithLifecycle(emptyList())
    val allTags by manageBookScreenModel.tags
      .collectAsStateWithLifecycle(emptyList())

    LaunchedEffect(pagerState.currentPage) {
      if (
        tabs[pagerState.currentPage] is ManageBookTab.Cover &&
        internetConnection == ConnectionState.Available
      ) {
        manageBookScreenModel.fetchCovers()
      }
    }

    val modalBottomSheetState = rememberModalBottomSheetState(
      initialValue = ModalBottomSheetValue.Hidden,
      skipHalfExpanded = true
    )

    var showContributorPickerDialog by remember { mutableStateOf(false) }
    val allPeople by manageBookScreenModel.people.collectAsStateWithLifecycle(emptyList())

    if (showContributorPickerDialog) {
      ContributorPickerDialog(
        allPeople = allPeople,
        contributor = manageBookScreenModel.selectedContributor,
        onDismiss = {
          showContributorPickerDialog = false
          manageBookScreenModel.selectedContributor = null
        },
        onFinish = { contributor ->
          manageBookScreenModel.handleContributor(contributor)
          showContributorPickerDialog = false
        }
      )
    }

    // Disable the back handling when writing to the database.
    BackHandler(manageBookScreenModel.writing) {}

    ModalBottomSheetLayout(
      sheetState = modalBottomSheetState,
      sheetShape = ModalBottomSheetExtraLargeShape,
      sheetBackgroundColor = Color.Transparent,
      sheetContent = {
        ContributorModalBottomSheet(
          contributor = manageBookScreenModel.selectedContributor,
          onEditClick = {
            scope.launch { modalBottomSheetState.hide() }
            showContributorPickerDialog = true
          },
          onRemoveClick = {
            scope.launch { modalBottomSheetState.hide() }
            manageBookScreenModel.removeSelectedContributor()
          }
        )
      }
    ) {
      Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
          EnhancedSmallTopAppBar(
            title = {
              Text(
                text = if (existingBookId != null) {
                  stringResource(R.string.edit_book)
                } else {
                  stringResource(R.string.create_book)
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            },
            scrollBehavior = scrollBehavior,
            navigationIcon = {
              IconButton(
                enabled = !manageBookScreenModel.writing,
                onClick = { navigator.pop() }
              ) {
                Icon(
                  imageVector = Icons.Outlined.ArrowBack,
                  contentDescription = stringResource(R.string.action_back)
                )
              }
            },
            contentPadding = WindowInsets.statusBars.asPaddingValues(),
            actions = {
              TextButton(
                enabled = !manageBookScreenModel.informationTabInvalid &&
                  !manageBookScreenModel.contributorsTabInvalid &&
                  !manageBookScreenModel.informationTabInvalid &&
                  !manageBookScreenModel.writing,
                onClick = {
                  if (manageBookScreenModel.mode == ManageBookScreenModel.Mode.CREATING) {
                    manageBookScreenModel.create { bookId ->
                      if (bookId != null) {
                        navigator.popUntil { it is LibraryScreen }
                        navigator.push(BookScreen(bookId))
                      }
                    }
                  } else {
                    manageBookScreenModel.edit {
                      navigator.pop()
                    }
                  }
                }
              ) {
                Text(stringResource(R.string.action_finish))
              }
            },
            content = {
              ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                edgePadding = 12.dp,
                containerColor = Color.Transparent,
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
            }
          )
        },
        content = { innerPadding ->
          HorizontalPager(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding),
            state = pagerState,
            count = tabs.size,
            verticalAlignment = Alignment.Top,
          ) { page ->
            when (tabs[page]) {
              ManageBookTab.Information -> {
                InformationTab(
                  code = manageBookScreenModel.code,
                  title = manageBookScreenModel.title,
                  synopsis = manageBookScreenModel.synopsis,
                  publisher = manageBookScreenModel.publisher,
                  publisherText = manageBookScreenModel.publisherText,
                  allPublishers = allPublishers,
                  pageCountText = manageBookScreenModel.pageCountText,
                  labelPriceCurrency = manageBookScreenModel.labelPriceCurrency,
                  labelPriceValue = manageBookScreenModel.labelPriceValue,
                  paidPriceCurrency = manageBookScreenModel.paidPriceCurrency,
                  paidPriceValue = manageBookScreenModel.paidPriceValue,
                  dimensionWidth = manageBookScreenModel.dimensionWidth,
                  dimensionHeight = manageBookScreenModel.dimensionHeight,
                  onCodeChange = { manageBookScreenModel.code = it },
                  onTitleChange = { manageBookScreenModel.title = it },
                  onSynopsisChange = { manageBookScreenModel.synopsis = it },
                  onPublisherTextChange = { manageBookScreenModel.publisherText = it },
                  onPublisherChange = { manageBookScreenModel.publisher = it },
                  onPageCountTextChange = { manageBookScreenModel.pageCountText = it },
                  onLabelPriceValueChange = { manageBookScreenModel.labelPriceValue = it },
                  onLabelPriceCurrencyChange = { manageBookScreenModel.labelPriceCurrency = it },
                  onPaidPriceValueChange = { manageBookScreenModel.paidPriceValue = it },
                  onPaidPriceCurrencyChange = { manageBookScreenModel.paidPriceCurrency = it },
                  onDimensionWidthChange = { manageBookScreenModel.dimensionWidth = it },
                  onDimensionHeightChange = { manageBookScreenModel.dimensionHeight = it }
                )
              }
              ManageBookTab.Contributors -> {
                ContributorsTab(
                  writing = manageBookScreenModel.writing,
                  contributors = manageBookScreenModel.contributors,
                  onAddContributorClick = { showContributorPickerDialog = true },
                  onContributorLongClick = { contributor ->
                    manageBookScreenModel.selectedContributor = contributor
                    scope.launch { modalBottomSheetState.show() }
                  }
                )
              }
              ManageBookTab.Organization -> {
                OrganizationTab(
                  store = manageBookScreenModel.store,
                  storeText = manageBookScreenModel.storeText,
                  allStores = allStores,
                  boughtAt = manageBookScreenModel.boughtAt,
                  group = manageBookScreenModel.group,
                  groupText = manageBookScreenModel.groupText,
                  allGroups = allGroups,
                  notes = manageBookScreenModel.notes,
                  isFuture = manageBookScreenModel.isFuture,
                  onStoreTextChange = { manageBookScreenModel.storeText = it },
                  onStoreChange = { manageBookScreenModel.store = it },
                  onBoughtAtChange = { manageBookScreenModel.boughtAt = it },
                  onGroupTextChange = { manageBookScreenModel.groupText = it },
                  onGroupChange = { manageBookScreenModel.group = it },
                  onNotesChange = { manageBookScreenModel.notes = it },
                  onIsFutureChange = { manageBookScreenModel.isFuture = it }
                )
              }
              ManageBookTab.Cover -> {
                CoverTab(
                  cover = manageBookScreenModel.cover,
                  allCovers = manageBookScreenModel.allCovers,
                  state = manageBookScreenModel.coverState,
                  canRefresh = manageBookScreenModel.coverRefreshEnabled() &&
                    internetConnection == ConnectionState.Available,
                  onChange = { manageBookScreenModel.cover = it },
                  onRefresh = { manageBookScreenModel.fetchCovers() },
                  onCustomCoverPicked = { customCover ->
                    manageBookScreenModel.cover = customCover
                    manageBookScreenModel.allCovers.add(customCover)
                  }
                )
              }
              ManageBookTab.Tags -> {
                TagsTab(
                  tags = manageBookScreenModel.rawTags,
                  allTags = allTags,
                  onTagsChange = { newTags ->
                    manageBookScreenModel.rawTags.clear()
                    manageBookScreenModel.rawTags.addAll(newTags)
                  }
                )
              }
            }
          }
        }
      )
    }
  }

  @Composable
  fun ContributorModalBottomSheet(
    contributor: Contributor?,
    onEditClick: () -> Unit,
    onRemoveClick: () -> Unit
  ) {
    ModalBottomSheet(
      header = {
        Column(modifier = Modifier.fillMaxWidth()) {
          ModalBottomSheetTitle(
            text = contributor?.personText.orEmpty(),
            contentPadding = PaddingValues(top = 24.dp, start = 24.dp, end = 24.dp)
          )
          Text(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
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
        }
      }
    ) {
      ModalBottomSheetItem(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(R.string.action_edit_contributor),
        icon = rememberVectorPainter(Icons.Outlined.Edit),
        onClick = onEditClick
      )
      ModalBottomSheetItem(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(R.string.action_remove_contributor),
        icon = rememberVectorPainter(Icons.Outlined.Delete),
        onClick = onRemoveClick
      )
      Spacer(
        modifier = Modifier
          .fillMaxWidth()
          .height(8.dp)
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

  @Parcelize
  sealed class ManageBookTab(@StringRes val title: Int) : Parcelable, Serializable {
    object Information : ManageBookTab(R.string.information)
    object Contributors : ManageBookTab(R.string.contributors)
    object Organization : ManageBookTab(R.string.organization)
    object Cover : ManageBookTab(R.string.cover)
    object Tags : ManageBookTab(R.string.tags)
  }

  companion object {
    private const val STEP_PICK_PERSON = 0
    private const val STEP_PICK_ROLE = 1
  }
  
}