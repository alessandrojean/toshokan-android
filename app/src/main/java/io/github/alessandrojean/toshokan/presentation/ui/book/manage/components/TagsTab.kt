package io.github.alessandrojean.toshokan.presentation.ui.book.manage.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explicit
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.NewLabel
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.database.data.Tag
import io.github.alessandrojean.toshokan.domain.RawTag
import io.github.alessandrojean.toshokan.presentation.ui.book.components.TagChip
import io.github.alessandrojean.toshokan.presentation.ui.core.components.NoItemsFound
import io.github.alessandrojean.toshokan.util.extension.bringIntoViewOnFocus

@Composable
fun TagsTab(
  tags: SnapshotStateList<RawTag>,
  allTags: List<Tag>,
  onTagsChange: (List<RawTag>) -> Unit
) {
  var tagText by rememberSaveable { mutableStateOf("") }
  val scope = rememberCoroutineScope()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .navigationBarsPadding()
      .imePadding(),
    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top)
  ) {
    if (tags.isEmpty()) {
      NoItemsFound(
        modifier = Modifier
          .fillMaxSize()
          .weight(1f),
        icon = Icons.Outlined.Label
      )
    } else {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .verticalScroll(rememberScrollState())
      ) {
        FlowRow(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          mainAxisSpacing = 8.dp,
          crossAxisSpacing = 8.dp
        ) {
          tags.forEach { tag ->
            TagChip(
              name = tag.tagText,
              isNsfw = tag.tag?.is_nsfw == true,
              onClick = {
                val newTags = tags.filter { it != tag }
                onTagsChange(newTags)
              }
            )
          }
        }
      }
    }

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
      expanded = expanded,
      onExpandedChange = { expanded = !expanded }
    ) {
      OutlinedTextField(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp, start = 12.dp, end = 12.dp)
          .bringIntoViewOnFocus(scope),
        value = tagText,
        onValueChange = { tagText = it },
        singleLine = true,
        label = { Text(stringResource(R.string.tag)) },
        leadingIcon = {
          Icon(
            imageVector = Icons.Outlined.NewLabel,
            contentDescription = stringResource(R.string.tag)
          )
        },
        trailingIcon = {
          ExposedDropdownMenuDefaults.TrailingIcon(expanded)
        },
        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
          onDone = {
            if (tagText.isBlank()) {
              return@KeyboardActions
            }

            val tagInBookTags = tags.any { it.tagText.equals(tagText, ignoreCase = true) }
            val existingTag = allTags.firstOrNull { it.name.equals(tagText, ignoreCase = true) }

            if (!tagInBookTags) {
              val newTag = RawTag(
                tag = existingTag,
                tagId = existingTag?.id,
                tagText = existingTag?.name ?: tagText
              )
              onTagsChange(tags.toList() + newTag)
            }

            tagText = ""
          }
        )
      )

      val filteringOptions = allTags.filter { tag ->
        tag.name.contains(tagText, ignoreCase = true) &&
          tags.firstOrNull { it.tagId == tag.id } == null
      }
      if (filteringOptions.isNotEmpty()) {
        ExposedDropdownMenu(
          expanded = expanded,
          onDismissRequest = { expanded = false }
        ) {
          filteringOptions.forEach { selectionOption ->
            DropdownMenuItem(
              text = {
                Text(
                  text = selectionOption.name,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              },
              trailingIcon = if (selectionOption.is_nsfw) {
                {
                  Icon(
                    painter = rememberVectorPainter(Icons.Outlined.Explicit),
                    contentDescription = null
                  )
                }
              } else null,
              onClick = {
                val newTag = RawTag(
                  tag = selectionOption,
                  tagId = selectionOption.id,
                  tagText = selectionOption.name
                )
                onTagsChange(tags.toList() + newTag)
                expanded = false
                tagText = ""
              }
            )
          }
        }
      }
    }
  }
}