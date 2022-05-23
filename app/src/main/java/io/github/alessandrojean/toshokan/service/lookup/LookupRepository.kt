package io.github.alessandrojean.toshokan.service.lookup

import io.github.alessandrojean.toshokan.service.lookup.cbl.CblLookup
import io.github.alessandrojean.toshokan.service.lookup.googlebooks.GoogleBooksLookup
import io.github.alessandrojean.toshokan.service.lookup.mercadoeditorial.MercadoEditorialLookup
import io.github.alessandrojean.toshokan.service.lookup.openlibrary.OpenLibraryLookup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface LookupRepository {
  suspend fun searchByIsbn(isbn: String): Map<Provider, List<LookupBookResult>>
}

@Singleton
class LookupRepositoryImpl @Inject constructor(
  cblLookup: CblLookup,
  googleBooksLookup: GoogleBooksLookup,
  mercadoEditorialLookup: MercadoEditorialLookup,
  openLibraryLookup: OpenLibraryLookup
) : LookupRepository {

  private val providers: List<LookupProvider> =
    listOf(cblLookup, googleBooksLookup, mercadoEditorialLookup, openLibraryLookup)

  override suspend fun searchByIsbn(isbn: String): Map<Provider, List<LookupBookResult>> {
    return withContext(Dispatchers.IO) {
      val apiCalls = providers.map { provider ->
        async {
          runCatching { provider.provider to provider.searchByIsbn(isbn) }
            .getOrElse { provider.provider to emptyList() }
        }
      }

      apiCalls.awaitAll()
        .filter { it.second.isNotEmpty() }
        .toMap()
    }
  }
}