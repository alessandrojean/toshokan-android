package io.github.alessandrojean.toshokan.presentation.ui.statistics.ranking

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import cafe.adriel.voyager.hilt.ScreenModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.alessandrojean.toshokan.domain.RankingItem
import io.github.alessandrojean.toshokan.presentation.ui.statistics.ranking.RankingScreen.RankingType
import io.github.alessandrojean.toshokan.repository.BooksRepository
import io.github.alessandrojean.toshokan.repository.GroupsRepository
import io.github.alessandrojean.toshokan.repository.PeopleRepository
import io.github.alessandrojean.toshokan.repository.PublishersRepository
import io.github.alessandrojean.toshokan.repository.StoresRepository
import io.github.alessandrojean.toshokan.repository.TagsRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class RankingScreenModel @AssistedInject constructor(
  private val booksRepository: BooksRepository,
  private val groupsRepository: GroupsRepository,
  private val peopleRepository: PeopleRepository,
  private val publishersRepository: PublishersRepository,
  private val storesRepository: StoresRepository,
  private val tagsRepository: TagsRepository,
  @Assisted("type") private val type: RankingType
) : StateScreenModel<RankingScreenModel.State>(State.Loading) {

  sealed class State {
    object Loading : State()
    data class Ranking(val ranking: List<RankingItem>) : State()
  }

  @AssistedFactory
  interface Factory : ScreenModelFactory {
    fun create(@Assisted("type") type: RankingType): RankingScreenModel
  }

  init {
    observeRanking()
  }

  private fun observeRanking() {
    val rankingFlow = when (type) {
      RankingType.PUBLISHER -> publishersRepository.subscribeToRanking()
      RankingType.AUTHOR -> peopleRepository.subscribeToAuthorRanking()
      RankingType.STORE -> storesRepository.subscribeToRanking()
      RankingType.COLLECTION -> booksRepository.subscribeToCollectionsRanking()
      RankingType.GROUP -> groupsRepository.subscribeToRanking()
      RankingType.TAG -> tagsRepository.subscribeToRanking()
    }

    coroutineScope.launch {
      rankingFlow
        .map { ranking -> ranking.filter { it.count > 0 } }
        .collect { mutableState.value = State.Ranking(it) }
    }
  }

}