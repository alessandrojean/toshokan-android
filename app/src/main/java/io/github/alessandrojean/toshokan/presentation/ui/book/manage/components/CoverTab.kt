package io.github.alessandrojean.toshokan.presentation.ui.book.manage.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.ui.core.components.NoItemsFound
import io.github.alessandrojean.toshokan.service.cover.CoverResult

@Composable
fun CoverTab(
  coverUrl: String,
  allCovers: List<CoverResult>,
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
          modifier = Modifier.fillMaxSize(),
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
              columns = GridCells.Adaptive(minSize = 128.dp),
              contentPadding = PaddingValues(12.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
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
        .padding(12.dp)
    ) {
      // TODO: Handle broken images and loading state.
      Image(
        painter = coverPainter,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .aspectRatio(2f / 3f)
          .clip(MaterialTheme.shapes.large)
      )

      Text(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
        text = stringResource(cover.source!!),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

sealed class CoverTabState {
  object Loading : CoverTabState()
  object Refreshing : CoverTabState()
  object Display : CoverTabState()
}