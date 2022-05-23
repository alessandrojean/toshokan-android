package io.github.alessandrojean.toshokan.presentation.ui.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.R

@Composable
fun MoreScreen(
  navigateToPublishers: () -> Unit,
  navigateToPeople: () -> Unit
) {
  Scaffold(
    topBar = {
      SmallTopAppBar(
        title = { Text(stringResource(R.string.more)) }
      )
    },
    content = { innerPadding ->
      Box(
        modifier = Modifier
          .padding(innerPadding)
          .fillMaxSize()
      ) {
        Column {
          NavigationItem(
            title = stringResource(R.string.publishers),
            icon = Icons.Outlined.Domain,
            onClick = navigateToPublishers
          )
          NavigationItem(
            title = stringResource(R.string.people),
            icon = Icons.Outlined.Group,
            onClick = navigateToPeople
          )
          NavigationItem(
            title = stringResource(R.string.stores),
            icon = Icons.Outlined.ShoppingCart,
            onClick = {}
          )
          NavigationItem(
            title = stringResource(R.string.groups),
            icon = Icons.Outlined.GroupWork,
            onClick = {}
          )
          NavigationItem(
            title = stringResource(R.string.settings),
            icon = Icons.Outlined.Settings,
            onClick = {}
          )
          NavigationItem(
            title = stringResource(R.string.about),
            icon = Icons.Outlined.Info,
            onClick = {}
          )
        }
      }
    }
  )
}

@Composable
fun NavigationItem(
  modifier: Modifier = Modifier,
  title: String,
  icon: ImageVector,
  onClick: () -> Unit,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clickable(
        onClick = onClick
      )
      .padding(16.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = title,
      tint = MaterialTheme.colorScheme.surfaceTint
    )
    Text(
      text = title,
      modifier = Modifier.padding(start = 24.dp)
    )
  }
}
