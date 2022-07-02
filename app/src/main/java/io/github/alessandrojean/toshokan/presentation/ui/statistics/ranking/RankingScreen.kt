package io.github.alessandrojean.toshokan.presentation.ui.statistics.ranking

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.alessandrojean.toshokan.R
import io.github.alessandrojean.toshokan.presentation.ui.core.components.BoxedCircularProgressIndicator
import io.github.alessandrojean.toshokan.presentation.ui.core.components.EnhancedSmallTopAppBar
import io.github.alessandrojean.toshokan.presentation.ui.statistics.components.RankingRow
import io.github.alessandrojean.toshokan.util.extension.collectAsStateWithLifecycle
import io.github.alessandrojean.toshokan.util.extension.plus

class RankingScreen(private val type: RankingType) : AndroidScreen() {

  override val key = "ranking_screen_${type.name}"

  enum class RankingType {
    AUTHOR,
    PUBLISHER,
    STORE,
    COLLECTION,
    GROUP,
    TAG
  }

  @Composable
  override fun Content() {
    val navigator = LocalNavigator.currentOrThrow
    val screenModel = getScreenModel<RankingScreenModel, RankingScreenModel.Factory> {
      it.create(type)
    }
    val state by screenModel.state.collectAsStateWithLifecycle()

    val topAppBarScrollState = rememberTopAppBarScrollState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior(topAppBarScrollState) }
    val titleRes = remember(type) {
      when (type) {
        RankingType.AUTHOR -> R.string.ranking_authors
        RankingType.PUBLISHER -> R.string.ranking_publishers
        RankingType.STORE -> R.string.ranking_stores
        RankingType.COLLECTION -> R.string.ranking_collections
        RankingType.GROUP -> R.string.ranking_groups
        RankingType.TAG -> R.string.ranking_tags
      }
    }

    Scaffold(
      modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
      topBar = {
        EnhancedSmallTopAppBar(
          contentPadding = WindowInsets.statusBars.asPaddingValues(),
          scrollBehavior = scrollBehavior,
          navigationIcon = {
            IconButton(onClick = { navigator.pop() }) {
              Icon(
                painter = rememberVectorPainter(Icons.Outlined.ArrowBack),
                contentDescription = stringResource(R.string.action_back)
              )
            }
          },
          title = { Text(stringResource(titleRes)) },
        )
      },
      content = { innerPadding ->
        val padding = innerPadding + WindowInsets.navigationBars.asPaddingValues()

        Crossfade(targetState = state) { state ->
          when (state) {
            RankingScreenModel.State.Loading -> {
              BoxedCircularProgressIndicator(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(padding)
              )
            }
            is RankingScreenModel.State.Ranking -> {
              LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = padding,
                state = rememberLazyListState()
              ) {
                items(state.ranking.size) { index ->
                  RankingRow(
                    modifier = Modifier.fillMaxWidth(),
                    position = index + 1,
                    item = state.ranking[index]
                  )
                }
              }
            }
          }
        }
      }
    )
  }

}