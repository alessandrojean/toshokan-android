package io.github.alessandrojean.toshokan.util.extension

import com.fredporciuncula.flow.preferences.Preference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

fun <T> Preference<T>.asImmediateFlow(block: (T) -> Unit): Flow<T> {
  block(get())
  return asFlow().onEach { block(it) }
}
