package io.github.alessandrojean.toshokan.presentation.ui.search.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.R

@Composable
fun SearchFilterChipsRow(
  modifier: Modifier = Modifier,
  shape: Shape = MaterialTheme.shapes.small,
  isFuture: Boolean?,
  favoritesOnly: Boolean,
  collectionsSelected: Boolean,
  showCollections: Boolean = true,
  groupsSelected: Boolean,
  showGroups: Boolean = true,
  contributorsSelected: Boolean,
  showContributors: Boolean = true,
  publishersSelected: Boolean,
  showPublishers: Boolean = true,
  storesSelected: Boolean,
  showStores: Boolean = true,
  boughtAtSelected: Boolean = false,
  readAtSelected: Boolean = false,
  onIsFutureChanged: (Boolean?) -> Unit,
  onFavoritesOnlyChanged: (Boolean) -> Unit,
  onCollectionsClick: () -> Unit,
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
    if (showCollections) {
      item("collections") {
        FilterChip(
          shape = shape,
          selected = collectionsSelected,
          onClick = { onCollectionsClick.invoke() },
          label = { Text(stringResource(R.string.filter_collection)) },
          leadingIcon = {
            Icon(
              imageVector = Icons.Outlined.CollectionsBookmark,
              contentDescription = null,
              modifier = Modifier.size(FilterChipDefaults.IconSize)
            )
          },
          selectedIcon = {
            Icon(
              imageVector = Icons.Outlined.Check,
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
    if (showContributors) {
      item("contributors") {
        FilterChip(
          shape = shape,
          selected = contributorsSelected,
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
            Icon(
              imageVector = Icons.Outlined.Check,
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
    if (showPublishers) {
      item("publishers") {
        FilterChip(
          shape = shape,
          selected = publishersSelected,
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
            Icon(
              imageVector = Icons.Outlined.Check,
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
    if (showGroups) {
      item("groups") {
        FilterChip(
          shape = shape,
          selected = groupsSelected,
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
            Icon(
              imageVector = Icons.Outlined.Check,
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
    if (showStores) {
      item("stores") {
        FilterChip(
          shape = shape,
          selected = storesSelected,
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
            Icon(
              imageVector = Icons.Outlined.Check,
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
