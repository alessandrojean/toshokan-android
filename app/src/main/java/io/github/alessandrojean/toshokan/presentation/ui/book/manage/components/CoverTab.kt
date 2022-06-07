package io.github.alessandrojean.toshokan.presentation.ui.book.manage.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.extensions.surfaceWithTonalElevation
import io.github.alessandrojean.toshokan.presentation.ui.core.components.NoItemsFound
import io.github.alessandrojean.toshokan.service.cover.CoverResult

@Composable
fun CoverTab(
  coverUrl: String,
  allCovers: SnapshotStateList<CoverResult>,
  state: CoverTabState = CoverTabState.Display,
  canRefresh: Boolean = true,
  onChange: (CoverResult) -> Unit,
  onRefresh: () -> Unit,
) {
  val gridState = rememberLazyGridState()
  val scrollState = rememberScrollState()

  val refreshing by remember(state) {
    derivedStateOf { state is CoverTabState.Refreshing }
  }

  val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = refreshing)

  Crossfade(
    targetState = state,
    modifier = Modifier.fillMaxSize()
  ) { coverState ->
    when (coverState) {
      is CoverTabState.Loading -> {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator()
        }
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
                bottom = 4.dp + WindowInsets.navigationBars
                  .asPaddingValues()
                  .calculateBottomPadding()
              ),
              verticalArrangement = Arrangement.spacedBy(4.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              items(allCovers, key = { it.imageUrl }) { cover ->
                CoverCard(
                  cover = cover,
                  enabled = !refreshing,
                  selected = coverUrl == cover.imageUrl,
                  onClick = { onChange(cover) }
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
}

@Composable
fun CoverCard(
  cover: CoverResult,
  enabled: Boolean,
  selected: Boolean,
  onClick: () -> Unit
) {
  var imageLoaded by remember { mutableStateOf(false) }

  val coverPainter = rememberAsyncImagePainter(
    model = cover.imageUrl,
    contentScale = ContentScale.Crop
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
        MaterialTheme.colorScheme.surfaceVariant
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

        AsyncImage(
          model = ImageRequest.Builder(LocalContext.current)
            .data(cover.imageUrl)
            .crossfade(true)
            .build(),
          contentDescription = null,
          contentScale = ContentScale.Inside,
          modifier = Modifier.clip(MaterialTheme.shapes.large),
          onSuccess = { imageLoaded = true }
        )
      }

      Text(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 4.dp),
        text = stringResource(cover.source!!),
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