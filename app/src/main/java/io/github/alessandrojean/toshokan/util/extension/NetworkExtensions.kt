package io.github.alessandrojean.toshokan.util.extension

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.URLBuilder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

suspend fun HttpResponse.document(): Document {
  return Jsoup.parse(bodyAsText(), request.url.toString())
}

fun postRequest(block: HttpRequestBuilder.() -> Unit) = HttpRequestBuilder().apply {
  method = HttpMethod.Post
  block()
}

fun getRequest(block: HttpRequestBuilder.() -> Unit) = HttpRequestBuilder().apply(block)

fun getRequest(url: String) = getRequest { url(url) }

fun HttpRequestBuilder.headers(headers: Headers) {
  this.headers.appendAll(headers)
}

fun HttpRequestBuilder.urlWithBuilder(urlString: String, block: URLBuilder.(URLBuilder) -> Unit) {
  url(urlString)
  url(block)
}