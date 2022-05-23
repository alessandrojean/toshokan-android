package io.github.alessandrojean.toshokan.network

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okhttp3.internal.closeQuietly
import java.io.IOException
import kotlin.coroutines.resumeWithException

inline fun <reified T> Response.parseAs(): T {
  val json = Json { ignoreUnknownKeys = true }

  this.use {
    val responseBody = it.body?.string().orEmpty()
    return json.decodeFromString(responseBody)
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun Call.awaitSuccess(): Response {
  return suspendCancellableCoroutine { continuation ->
    enqueue(object : Callback {
      override fun onResponse(call: Call, response: Response) {
        if (response.isSuccessful) {
          continuation.resume(response) {
            response.body?.closeQuietly()
          }
        } else {
          continuation.resumeWithException(Exception("Erro HTTP ${response.code}"))
        }
      }

      override fun onFailure(call: Call, e: IOException) {
        continuation.resumeWithException(e)
      }
    })

    continuation.invokeOnCancellation { cancel() }
  }
}