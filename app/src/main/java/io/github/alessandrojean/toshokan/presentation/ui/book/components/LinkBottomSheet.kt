package io.github.alessandrojean.toshokan.presentation.ui.book.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Feed
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.ui.theme.DividerOpacity
import io.github.alessandrojean.toshokan.service.link.BookLink
import io.github.alessandrojean.toshokan.service.link.LinkCategory

@Composable
fun LinkBottomSheet(
  modifier: Modifier = Modifier,
  links: Map<LinkCategory, List<BookLink>>,
  onLinkClick: (BookLink) -> Unit
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .then(modifier),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 12.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
    ) {
      Text(
        modifier = Modifier.padding(top = 24.dp, start = 24.dp, end = 24.dp),
        text = stringResource(R.string.links),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.titleLarge
      )
      Divider(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 16.dp, bottom = 8.dp),
        color = LocalContentColor.current.copy(alpha = DividerOpacity)
      )

      links.entries.forEachIndexed { index, (category, links) ->
        if (index > 0) {
          Divider(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp),
            color = LocalContentColor.current.copy(alpha = DividerOpacity)
          )
        }

        links.forEach { link ->
          ModalBottomSheetItem(
            modifier = Modifier.fillMaxWidth(),
            icon = when {
              link.icon != null -> painterResource(link.icon)
              category == LinkCategory.STORE -> rememberVectorPainter(Icons.Outlined.LocalMall)
              else -> rememberVectorPainter(Icons.Outlined.Feed)
            },
            text = stringResource(link.name),
            onClick = { onLinkClick.invoke(link) }
          )
        }
      }
      Spacer(
        modifier = Modifier
          .fillMaxWidth()
          .height(8.dp)
      )
    }
  }
}

@Composable
fun ModalBottomSheetItem(
  modifier: Modifier = Modifier,
  text: String,
  icon: Painter = rememberVectorPainter(Icons.Outlined.Link),
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
      painter = icon,
      contentDescription = text,
      tint = LocalContentColor.current
    )
    Text(
      text = text,
      modifier = Modifier.padding(start = 24.dp),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}