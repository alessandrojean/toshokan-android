package io.github.alessandrojean.toshokan.presentation.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import io.github.alessandrojean.toshokan.presentation.ui.book.BookScreen
import io.github.alessandrojean.toshokan.presentation.ui.createbook.CreateBookScreen
import io.github.alessandrojean.toshokan.presentation.ui.library.LibraryScreen
import io.github.alessandrojean.toshokan.presentation.ui.more.MoreScreen
import io.github.alessandrojean.toshokan.presentation.ui.publishers.PublishersScreen
import io.github.alessandrojean.toshokan.presentation.ui.statistics.StatisticsScreen

@Composable
internal fun Navigation(
  navController: NavHostController,
  startScreen: TopScreen,
  requestHideNavigator: (Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  NavHost(
    navController = navController,
    startDestination = startScreen.route,
    modifier = modifier
  ) {
    addLibraryTopLevel(navController, requestHideNavigator)
    addStatisticsTopLevel(navController)
    addMoreTopLevel(navController)
  }
}

private fun NavGraphBuilder.addLibraryTopLevel(
  navController: NavController,
  requestHideNavigator: (Boolean) -> Unit
) {
  val topScreen = TopScreen.Library

  navigation(
    route = topScreen.route,
    startDestination = LeafScreen.Library.createRoute(topScreen)
  ) {
    addLibrary(navController, topScreen, requestHideNavigator)
    addBook(navController, topScreen)
    addCreateBook(navController, topScreen)
  }
}

private fun NavGraphBuilder.addStatisticsTopLevel(
  navController: NavController
) {
  val topScreen = TopScreen.Statistics

  navigation(
    route = topScreen.route,
    startDestination = LeafScreen.Statistics.createRoute(topScreen)
  ) {
    addStatistics(navController, topScreen)
  }
}

private fun NavGraphBuilder.addMoreTopLevel(
  navController: NavController
) {
  val topScreen = TopScreen.More

  navigation(
    route = topScreen.route,
    startDestination = LeafScreen.More.createRoute(topScreen)
  ) {
    addMore(navController, topScreen)
    addPublishersList(navController, topScreen)
  }
}

private fun NavGraphBuilder.addLibrary(
  navController: NavController,
  root: TopScreen,
  requestHideNavigator: (Boolean) -> Unit
) {
  composable(LeafScreen.Library.createRoute(root)) {
    LibraryScreen(
      requestHideNavigator = requestHideNavigator,
      openBook = { bookId ->
        navController.navigate(LeafScreen.Book.createRoute(root, bookId))
      },
      createNewBook = {
        navController.navigate(LeafScreen.CreateBook.createRoute(root))
      }
    )
  }
}

private fun NavGraphBuilder.addStatistics(
  navController: NavController,
  root: TopScreen
) {
  composable(LeafScreen.Statistics.createRoute(root)) {
    StatisticsScreen()
  }
}

private fun NavGraphBuilder.addMore(
  navController: NavController,
  root: TopScreen
) {
  composable(LeafScreen.More.createRoute(root)) {
    MoreScreen(
      navigateToPublishers = {
        navController.navigate(LeafScreen.Publishers.createRoute(root))
      }
    )
  }
}

private fun NavGraphBuilder.addBook(
  navController: NavController,
  root: TopScreen
) {
  composable(LeafScreen.Book.createRoute(root)) { entry ->
    val bookId = entry.arguments?.getLong("bookId") as Long
    BookScreen(bookId = bookId)
  }
}

private fun NavGraphBuilder.addCreateBook(
  navController: NavController,
  root: TopScreen
) {
  composable(LeafScreen.CreateBook.createRoute(root)) {
    CreateBookScreen(
      navController,
      createBookViewModel = hiltViewModel()
    )
  }
}

private fun NavGraphBuilder.addPublishersList(
  navController: NavController,
  root: TopScreen
) {
  composable(LeafScreen.Publishers.createRoute(root)) {
    PublishersScreen(
      navController,
      publishersViewModel = hiltViewModel()
    )
  }
}