package io.github.alessandrojean.toshokan.presentation.ui.main

sealed class TopScreen(val route: String) {
  object Library: TopScreen("library")
  object Statistics : TopScreen("statistics" )
  object More : TopScreen("more")
}

sealed class LeafScreen(private val route: String) {
  fun createRoute(root: TopScreen) = "${root.route}/$route"

  object Library : LeafScreen("library")

  object Book : LeafScreen("book/{bookId}") {
    fun createRoute(root: TopScreen, bookId: Long) = "${root.route}/book/$bookId"
  }

  object CreateBook : LeafScreen("new_book")

  object Statistics : LeafScreen("statistics")

  object More : LeafScreen("more")

  object Publishers : LeafScreen("publishers")

  object Stores : LeafScreen("stores")

  object Groups : LeafScreen("groups")
}
