package io.github.alessandrojean.toshokan.presentation.ui.book.manage.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.extensions.surfaceWithTonalElevation
import io.github.alessandrojean.toshokan.presentation.extensions.withTonalElevation
import io.github.alessandrojean.toshokan.presentation.ui.core.components.BoxedCircularProgressIndicator
import io.github.alessandrojean.toshokan.presentation.ui.core.components.NoItemsFound
import io.github.alessandrojean.toshokan.service.cover.BookCover

@Composable
fun CoverTab(
  cover: BookCover?,
  allCovers: SnapshotStateList<BookCover>,
  state: CoverTabState = CoverTabState.Display,
  canRefresh: Boolean = true,
  onChange: (BookCover?) -> Unit,
  onRefresh: () -> Unit,
  onCustomCoverPicked: (BookCover.Custom) -> Unit = {},
) {
  val gridState = rememberLazyGridState()
  val scrollState = rememberScrollState()

  val refreshing by remember(state) {
    derivedStateOf { state is CoverTabState.Refreshing }
  }

  val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = refreshing)
  
  var customCover by remember { mutableStateOf<BookCover.Custom?>(null) }
  val customCoverPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent(),
    onResult = { uri ->
      uri?.let {
        customCover = BookCover.Custom(uri = it)
        onCustomCoverPicked.invoke(customCover!!)
      }
    }
  )
  
  Scaffold(
    modifier = Modifier.fillMaxSize(),
    floatingActionButton = {
      FloatingActionButton(
        onClick = { customCoverPickerLauncher.launch("image/*") }
      ) {
        Icon(
          imageVector = Icons.Outlined.AddPhotoAlternate,
          contentDescription = stringResource(R.string.action_add)
        )
      }
    },
    content = { innerPadding ->
      Crossfade(
        targetState = state,
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) { coverState ->
        when (coverState) {
          is CoverTabState.Loading -> {
            BoxedCircularProgressIndicator(
              modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
            )
          }
          is CoverTabState.Display,
          is CoverTabState.Refreshing -> {
            SwipeRefresh(
              state = swipeRefreshState,
              swipeEnabled = canRefresh,
              indicator = { state, trigger ->
                SwipeRefreshIndicator(
                  state = state,
                  refreshTriggerDistance = trigger,
                  backgroundColor = MaterialTheme.colorScheme.primary,
                  contentColor = MaterialTheme.colorScheme.onPrimary
                )
              },
              onRefresh = onRefresh
            ) {
              if (allCovers.isNotEmpty()) {
                LazyVerticalGrid(
                  modifier = Modifier.selectableGroup(),
                  state = gridState,
                  columns = GridCells.Adaptive(minSize = 96.dp),
                  contentPadding = PaddingValues(
                    top = 4.dp,
                    start = 4.dp,
                    end = 4.dp,
                    bottom = 4.dp + innerPadding.calculateBottomPadding()
                  ),
                  verticalArrangement = Arrangement.spacedBy(4.dp),
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  items(allCovers, contentType = { it.javaClass.canonicalName }) { coverOption ->
                    val selected = cover == coverOption

                    CoverCard(
                      cover = coverOption,
                      enabled = !refreshing,
                      selected = selected,
                      onClick = {
                        onChange.invoke(if (!selected) coverOption else null)
                      }
                    )
                  }
                }
              } else {
                NoItemsFound(
                  modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                  text = stringResource(R.string.no_covers_found),
                  icon = Icons.Outlined.ImageSearch
                )
              }
            }
          }
        }
      }
    },
    bottomBar = {
      Spacer(
        modifier = Modifier.windowInsetsPadding(
          WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
        )
      )
    }
  )
}

@Composable
fun CoverCard(
  cover: BookCover,
  enabled: Boolean,
  selected: Boolean,
  onClick: () -> Unit
) {
  var imageLoaded by remember { mutableStateOf(false) }
  var aspectRatio by remember { mutableStateOf(2f / 3f) }

  val selectedColor = MaterialTheme.colorScheme.surfaceTint

  val borderWidth by animateDpAsState(if (selected) 2.dp else 0.dp)
  val borderColor by animateColorAsState(
    if (selected) selectedColor else Color.Transparent
  )

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .selectable(
        selected = selected,
        enabled = enabled,
        role = Role.RadioButton,
        onClick = onClick
      ),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(
      containerColor = if (selected) {
        MaterialTheme.colorScheme.surfaceVariant.withTonalElevation(4.dp)
      } else {
        MaterialTheme.colorScheme.surface
      }
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(4.dp)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(2f / 3f),
        contentAlignment = Alignment.Center
      ) {
        androidx.compose.animation.AnimatedVisibility(
          visible = !imageLoaded,
          enter = fadeIn(),
          exit = fadeOut()
        ) {
          Box(
            modifier = Modifier
              .clip(MaterialTheme.shapes.large)
              .fillMaxSize()
              .background(MaterialTheme.colorScheme.surfaceWithTonalElevation(6.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Outlined.Book,
              contentDescription = null,
              tint = LocalContentColor.current.copy(alpha = 0.5f)
            )
          }
        }

        val dataUrl = when (cover) {
          is BookCover.External -> cover.imageUrl
          is BookCover.Custom -> cover.uri
        }

        AsyncImage(
          model = ImageRequest.Builder(LocalContext.current)
            .data(dataUrl)
            .crossfade(true)
            .build(),
          contentDescription = null,
          contentScale = ContentScale.Fit,
          modifier = Modifier
            .aspectRatio(aspectRatio)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .drawWithContent {
              drawContent()
              if (selected) {
                drawRect(
                  color = selectedColor.copy(alpha = 0.35f),
                  topLeft = Offset.Zero,
                  size = size
                )
              }
            }
            .border(BorderStroke(borderWidth, borderColor), MaterialTheme.shapes.large),
          onSuccess = { state ->
            imageLoaded = true
            aspectRatio = state.painter.intrinsicSize.width / state.painter.intrinsicSize.height
          }
        )
      }

      Text(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 4.dp),
        text = when(cover) {
          is BookCover.Current -> stringResource(R.string.current_cover)
          is BookCover.Custom -> stringResource(R.string.custom_cover)
          is BookCover.Result -> stringResource(cover.source!!)
          else -> ""
        },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodyMedium
      )
    }
  }
}

sealed class CoverTabState {
  object Loading : CoverTabState()
  object Refreshing : CoverTabState()
  object Display : CoverTabState()
}