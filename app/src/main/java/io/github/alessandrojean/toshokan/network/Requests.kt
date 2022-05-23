package io.github.alessandrojean.toshokan.network

import okhttp3.CacheControl
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.Request
import okhttp3.RequestBody
import java.util.concurrent.TimeUnit.MINUTES

private val DEFAULT_CACHE_CONTROL = CacheControl.Builder().maxAge(10, MINUTES).build()
private val DEFAULT_HEADERS = Headers.Builder().build()
private val DEFAULT_BODY: RequestBody = FormBody.Builder().build()

fun GET(
  url: String,
  headers: Headers = DEFAULT_HEADERS,
  cache: CacheControl = DEFAULT_CACHE_CONTROL
): Request {
  return Request.Builder()
    .url(url)
    .headers(headers)
    .cacheControl(cache)
    .build()
}

fun POST(
  url: String,
  headers: Headers = DEFAULT_HEADERS,
  body: RequestBody = DEFAULT_BODY,
  cache: CacheControl = DEFAULT_CACHE_CONTROL
): Request {
  return Request.Builder()
    .url(url)
    .post(body)
    .headers(headers)
    .cacheControl(cache)
    .build()
}
