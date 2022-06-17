package io.github.alessandrojean.toshokan.presentation.ui.book.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.BadgedBox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.FirstPage
import androidx.compose.material.icons.outlined.LastPage
import androidx.compose.material3.Badge
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.domain.BookNeighbors
import io.github.alessandrojean.toshokan.presentation.extensions.surfaceWithTonalElevation
import io.github.alessandrojean.toshokan.presentation.ui.theme.DividerOpacity

@Composable
fun BookCollectionBottomPagination(
  modifier: Modifier = Modifier,
  tonalElevation: Dp = 12.dp,
  visible: Boolean = true,
  bookNeighbors: BookNeighbors?,
  onCollectionClick: () -> Unit,
  onFirstClick: () -> Unit,
  onLastClick: () -> Unit,
  onPreviousClick: () -> Unit,
  onNextClick: () -> Unit
) {
  AnimatedVisibility(
    visible = bookNeighbors != null && visible,
    enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
    exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
  ) {
    Column(
      modifier = Modifier
        .background(MaterialTheme.colorScheme.surfaceWithTonalElevation(tonalElevation))
        .then(modifier)
    ) {
      Divider(color = LocalContentColor.current.copy(alpha = DividerOpacity))
      BottomAppBar(tonalElevation = tonalElevation) {
        IconButton(onClick = onCollectionClick) {
          BadgedBox(
            badge = {
              Badge {
                Text(bookNeighbors!!.count.toString())
              }
            }
          ) {
            Icon(
              imageVector = Icons.Outlined.CollectionsBookmark,
              contentDescription = stringResource(R.string.action_search)
            )
          }
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
          onClick = onFirstClick,
          enabled = bookNeighbors?.first != null &&
            bookNeighbors.first.id != bookNeighbors.current?.id
        ) {
          Icon(
            imageVector = Icons.Outlined.FirstPage,
            contentDescription = stringResource(R.string.action_first)
          )
        }
        IconButton(
          onClick = onPreviousClick,
          enabled = bookNeighbors?.previous != null
        ) {
          Icon(
            imageVector = Icons.Outlined.ChevronLeft,
            contentDescription = stringResource(R.string.action_previous)
          )
        }
        IconButton(
          onClick = onNextClick,
          enabled = bookNeighbors?.next != null
        ) {
          Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = stringResource(R.string.action_next)
          )
        }
        IconButton(
          onClick = onLastClick,
          enabled = bookNeighbors?.last != null &&
            bookNeighbors.last.id != bookNeighbors.current?.id
        ) {
          Icon(
            imageVector = Icons.Outlined.LastPage,
            contentDescription = stringResource(R.string.action_last)
          )
        }
      }
    }
  }
}
