package io.github.alessandrojean.toshokan.util.extension

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

suspend fun HttpResponse.document(): Document {
  return Jsoup.parse(bodyAsText(), request.url.toString())
}
