package io.github.alessandrojean.toshokan.service.lookup

import io.github.alessandrojean.toshokan.service.lookup.cbl.CblLookup
import io.github.alessandrojean.toshokan.service.lookup.googlebooks.GoogleBooksLookup
import io.github.alessandrojean.toshokan.service.lookup.mercadoeditorial.MercadoEditorialLookup
import io.github.alessandrojean.toshokan.service.lookup.openlibrary.OpenLibraryLookup
import io.github.alessandrojean.toshokan.service.lookup.skoob.SkoobLookup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

interface LookupRepository {
  fun searchByIsbn(isbn: String): Flow<LookupResult>
}

sealed class LookupResult {
  object Loading : LookupResult()
  data class Error(
    val error: Throwable,
    val progress: Float,
    val lastResults: List<LookupBookResult>
  ) : LookupResult()
  data class Result(val progress: Float, val results: List<LookupBookResult>) : LookupResult()
}

typealias SearchResult = Result<List<LookupBookResult>>

@Singleton
class LookupRepositoryImpl @Inject constructor(
  cblLookup: CblLookup,
  googleBooksLookup: GoogleBooksLookup,
  mercadoEditorialLookup: MercadoEditorialLookup,
  openLibraryLookup: OpenLibraryLookup,
  skoobLookup: SkoobLookup
) : LookupRepository {

  private val providers: List<LookupProvider> = listOf(
    cblLookup, googleBooksLookup, mercadoEditorialLookup, openLibraryLookup, skoobLookup
  )

  override fun searchByIsbn(isbn: String): Flow<LookupResult> {
    val progressSlice = 1f / providers.size.toFloat()

    val searchFlow = channelFlow {
      coroutineScope {
        providers.forEach { provider ->
          launch(Dispatchers.IO) {
            send(runCatching { provider.searchByIsbn(isbn) })
          }
        }
      }
    }

    return searchFlow
      .cancellable()
      .runningFold<SearchResult, LookupResult>(LookupResult.Loading) { acc, value ->
        when {
          acc is LookupResult.Loading && value.isSuccess ->
            LookupResult.Result(progressSlice, value.getOrNull()!!)
          acc is LookupResult.Loading && value.isFailure ->
            LookupResult.Error(value.exceptionOrNull()!!, progressSlice, emptyList())
          acc is LookupResult.Result && value.isSuccess ->
            LookupResult.Result(
              progress = acc.progress + progressSlice,
              results = acc.results + value.getOrNull()!!
            )
          acc is LookupResult.Result && value.isFailure ->
            LookupResult.Error(
              error = value.exceptionOrNull()!!,
              progress = acc.progress + progressSlice,
              lastResults = acc.results
            )
          acc is LookupResult.Error && value.isSuccess ->
            LookupResult.Result(
              progress = acc.progress + progressSlice,
              results = acc.lastResults + value.getOrNull()!!
            )
          acc is LookupResult.Error && value.isFailure ->
            LookupResult.Error(
              error = value.exceptionOrNull()!!,
              progress = acc.progress + progressSlice,
              lastResults = acc.lastResults
            )
          else -> acc
        }
      }
  }

//  override fun searchByIsbn(isbn: String): Flow<LookupResult> = channelFlow {
//    val results = mutableListOf<LookupBookResult>()
//    val progressSlice = 1f / providers.size.toFloat()
//    var progress = 0f
//
//    send(LookupResult.Loading)
//
//    providers.forEach { provider ->
//      launch(Dispatchers.IO) {
//        val providerResult = runCatching { provider.searchByIsbn(isbn) }
//        progress += progressSlice
//
//        if (providerResult.isSuccess) {
//          results += providerResult.getOrNull()!!
//          send(LookupResult.Result(progress, results))
//        } else {
//          send(LookupResult.Error(providerResult.exceptionOrNull()!!, results))
//        }
//
//        if (progress == 1f) {
//          send(LookupResult.Finish(results))
//        }
//      }
//    }
//  }

//  override suspend fun searchByIsbn(isbn: String): Map<Provider, List<LookupBookResult>> {
//    return withContext(Dispatchers.IO) {
//      val apiCalls = providers.map { provider ->
//        async {
//          runCatching { provider.provider to provider.searchByIsbn(isbn) }
//            .getOrElse { provider.provider to emptyList() }
//        }
//      }
//
//      apiCalls.awaitAll()
//        .filter { it.second.isNotEmpty() }
//        .toMap()
//    }
//  }
}