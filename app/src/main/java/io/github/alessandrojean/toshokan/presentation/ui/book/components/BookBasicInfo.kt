package io.github.alessandrojean.toshokan.presentation.ui.book.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.extensions.withTonalElevation
import io.github.alessandrojean.toshokan.presentation.ui.core.components.ExpandedIconButton
import io.github.alessandrojean.toshokan.util.extension.placeholder

@Composable
fun BookBasicInfo(
  modifier: Modifier = Modifier,
  placeholder: Boolean = false,
  title: String,
  authors: String,
  isRead: Boolean,
  isFuture: Boolean,
  inLibrary: Boolean,
  tonalElevation: Dp = 6.dp,
  buttonRowCorner: Dp = 18.dp,
  buttonRowContentPadding: PaddingValues = PaddingValues(12.dp),
  buttonRowContainerColor: Color = MaterialTheme.colorScheme
    .surfaceVariant.withTonalElevation(tonalElevation),
  onAddToLibraryClick: () -> Unit,
  onReadingClick: () -> Unit,
  onEditClick: () -> Unit,
  onDeleteClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .then(modifier)
  ) {
    Text(
      modifier = Modifier
        .padding(horizontal = 24.dp)
        .semantics { heading() }
        .placeholder(placeholder),
      text = title.ifEmpty { "Book title" },
      style = MaterialTheme.typography.headlineSmall
    )
    Text(
      modifier = Modifier
        .padding(start = 24.dp, end = 24.dp, top = 2.dp)
        .placeholder(placeholder),
      text = authors.ifEmpty { "Book authors" },
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      style = MaterialTheme.typography.bodyLarge.copy(
        color = LocalContentColor.current.copy(alpha = 0.8f)
      )
    )
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 18.dp),
      horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      val buttonColors = ButtonDefaults.textButtonColors(
        containerColor = buttonRowContainerColor,
        contentColor = LocalContentColor.current,
        disabledContainerColor = buttonRowContainerColor.copy(alpha = 0.25f)
      )

      if (!inLibrary) {
        ExpandedIconButton(
          modifier = Modifier.weight(1f),
          icon = Icons.Outlined.Add,
          text = stringResource(R.string.action_add_to_library),
          colors = buttonColors,
          shape = RoundedCornerShape(
            topStart = buttonRowCorner,
            bottomStart = buttonRowCorner
          ),
          contentPadding = buttonRowContentPadding,
          enabled = !placeholder && !inLibrary,
          onClick = onAddToLibraryClick,
        )
      }

      ExpandedIconButton(
        modifier = Modifier.weight(1f),
        icon = if (isRead) {
          Icons.Filled.Bookmarks
        } else {
          Icons.Outlined.Bookmarks
        },
        text = if (isRead) {
          stringResource(R.string.book_read)
        } else {
          stringResource(R.string.book_unread)
        },
        colors = buttonColors,
        shape = if (inLibrary) {
          RoundedCornerShape(
            topStart = buttonRowCorner,
            bottomStart = buttonRowCorner
          )
        } else {
          RoundedCornerShape(
            topEnd = buttonRowCorner,
            bottomEnd = buttonRowCorner
          )
        },
        contentPadding = buttonRowContentPadding,
        enabled = !isFuture && !placeholder && inLibrary,
        onClick = onReadingClick
      )

      if (inLibrary) {
        ExpandedIconButton(
          modifier = Modifier.weight(1f),
          icon = Icons.Outlined.Edit,
          text = stringResource(R.string.action_edit),
          colors = buttonColors,
          shape = RectangleShape,
          contentPadding = buttonRowContentPadding,
          enabled = !placeholder && inLibrary,
          onClick = onEditClick
        )
        ExpandedIconButton(
          modifier = Modifier.weight(1f),
          icon = Icons.Outlined.Delete,
          text = stringResource(R.string.action_delete),
          colors = buttonColors,
          shape = RoundedCornerShape(
            topEnd = buttonRowCorner,
            bottomEnd = buttonRowCorner
          ),
          contentPadding = buttonRowContentPadding,
          enabled = !placeholder && inLibrary,
          onClick = onDeleteClick,
        )
      }

    }
  }
}