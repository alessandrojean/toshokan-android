package io.github.alessandrojean.toshokan.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object HttpClient {
  val default: OkHttpClient by lazy {
    val logging = HttpLoggingInterceptor()
      .apply { level = HttpLoggingInterceptor.Level.BODY }

    OkHttpClient.Builder()
      .addInterceptor(logging)
      .build()
  }
}
