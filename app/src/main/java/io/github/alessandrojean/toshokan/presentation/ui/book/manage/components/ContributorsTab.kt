package io.github.alessandrojean.toshokan.presentation.ui.book.manage.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.domain.Contributor
import io.github.alessandrojean.toshokan.presentation.ui.core.components.NoItemsFound
import java.util.Locale

@Composable
fun ContributorsTab(
  writing: Boolean,
  contributors: List<Contributor>,
  onAddContributorClick: () -> Unit,
  onContributorLongClick: (Contributor) -> Unit
) {
  val listState = rememberLazyListState()

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    floatingActionButton = {
      AnimatedVisibility(
        visible = !writing,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        ExtendedFloatingActionButton(
          text = { Text(stringResource(R.string.action_add)) },
          icon = {
            Icon(
              imageVector = Icons.Outlined.Add,
              contentDescription = stringResource(R.string.action_add)
            )
          },
          expanded = listState.firstVisibleItemIndex == 0,
          onClick = onAddContributorClick
        )
      }
    },
    content = { innerPadding ->
      Crossfade(
        modifier = Modifier.fillMaxSize(),
        targetState = contributors.isEmpty()
      ) { isEmpty ->
        if (isEmpty) {
          NoItemsFound(
            modifier = Modifier.padding(innerPadding),
            text = stringResource(R.string.no_contributors),
            icon = Icons.Outlined.Group
          )
        } else {
          LazyColumn(
            contentPadding = innerPadding,
            state = listState
          ) {
            items(contributors) { contributor ->
              ContributorRow(
                modifier = Modifier
                  .fillMaxWidth()
                  .animateItemPlacement(),
                contributor = contributor,
                onLongClick = {
                  if (!writing) {
                    onContributorLongClick(contributor)
                  }
                }
              )
            }
          }
        }
      }
    },
    bottomBar = {
      Spacer(
        modifier = Modifier.windowInsetsPadding(
          WindowInsets.systemBars.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
          )
        )
      )
    }
  )
}

@Composable
fun ContributorRow(
  modifier: Modifier = Modifier,
  contributor: Contributor,
  onLongClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .combinedClickable(
        onClick = { /* Do nothing */ },
        onLongClick = onLongClick
      )
      .padding(vertical = 16.dp, horizontal = 12.dp)
      .then(modifier)
  ) {
    Text(
      text = contributor.personText,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
    Text(
      text = stringResource(
        R.string.person_role,
        stringResource(contributor.role.title).lowercase(Locale.getDefault())
      ),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      style = MaterialTheme.typography.bodySmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontStyle = FontStyle.Italic
      )
    )
  }
}