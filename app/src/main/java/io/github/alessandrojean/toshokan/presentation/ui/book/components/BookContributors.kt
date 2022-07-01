package io.github.alessandrojean.toshokan.presentation.ui.book.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.BookContributor
import io.github.alessandrojean.toshokan.presentation.extensions.withTonalElevation

@Composable
fun BookContributors(
  modifier: Modifier = Modifier,
  contributors: List<BookContributor> = emptyList(),
  minContributors: Int = 4,
  containerColor: Color = MaterialTheme.colorScheme.surface,
  tonalElevation: Dp = 6.dp,
  onContributorClick: (BookContributor) -> Unit
) {
  var contributorsExpanded by remember { mutableStateOf(false) }
  val contributorsToggleable by remember(contributors) {
    derivedStateOf { contributors.size > minContributors }
  }
  val visibleContributors by remember(contributors, contributorsExpanded) {
    derivedStateOf {
      if (contributorsExpanded) {
        contributors
      } else {
        contributors.take(minContributors)
      }
    }
  }
  val contributorsButtonHeight = 58f
  val contributorsIconRotation by animateFloatAsState(if (contributorsExpanded) 180f else 0f)
  val contributorsBackground = containerColor.withTonalElevation(tonalElevation)

  if (contributors.isNotEmpty()) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .then(modifier)
    ) {
      Text(
        modifier = Modifier
          .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 12.dp)
          .semantics { heading() },
        text = stringResource(R.string.contributors),
        style = MaterialTheme.typography.titleLarge
      )
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .animateContentSize()
      ) {
        visibleContributors.forEach { contributor ->
          BookContributorRow(
            contributor = contributor,
            onClick = { onContributorClick(contributor) }
          )
        }
        if (contributorsToggleable) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(contributorsButtonHeight.dp)
          ) {
            if (!contributorsExpanded) {
              BookContributorRow(contributor = contributors[minContributors])
            }
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(
                  Brush.verticalGradient(
                    0.0f to Color.Transparent,
                    0.2f to contributorsBackground.copy(alpha = 0.5f),
                    0.8f to contributorsBackground
                  )
                )
                .toggleable(
                  value = contributorsExpanded,
                  onValueChange = { contributorsExpanded = it },
                  role = Role.Checkbox,
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                modifier = Modifier.graphicsLayer(rotationX = contributorsIconRotation),
                imageVector = Icons.Outlined.ExpandMore,
                contentDescription = null
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun BookContributorRow(
  modifier: Modifier = Modifier,
  contributor: BookContributor,
  onClick: (() -> Unit)? = null
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(
        enabled = onClick != null,
        onClick = onClick ?: {}
      )
      .padding(horizontal = 24.dp, vertical = 10.dp)
      .then(modifier),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      modifier = Modifier
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.surfaceVariant.withTonalElevation(6.dp))
        .padding(6.dp)
        .size(24.dp),
      imageVector = Icons.Outlined.Person,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Column(
      modifier = Modifier
        .padding(start = 16.dp)
        .weight(1f)
    ) {
      Text(
        modifier = Modifier.fillMaxWidth(),
        text = contributor.person_name,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Text(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(contributor.role.title),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}
