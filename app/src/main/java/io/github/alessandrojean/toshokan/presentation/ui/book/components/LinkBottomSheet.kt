package io.github.alessandrojean.toshokan.presentation.ui.book.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Feed
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material3.Divider
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.ui.core.components.ModalBottomSheet
import io.github.alessandrojean.toshokan.presentation.ui.core.components.ModalBottomSheetItem
import io.github.alessandrojean.toshokan.presentation.ui.theme.DividerOpacity
import io.github.alessandrojean.toshokan.service.link.BookLink
import io.github.alessandrojean.toshokan.service.link.LinkCategory

@Composable
fun LinkBottomSheet(
  modifier: Modifier = Modifier,
  links: Map<LinkCategory, List<BookLink>>,
  onLinkClick: (BookLink) -> Unit
) {
  ModalBottomSheet(
    modifier = modifier,
    tonalElevation = 12.dp,
    title = stringResource(R.string.links)
  ) {
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