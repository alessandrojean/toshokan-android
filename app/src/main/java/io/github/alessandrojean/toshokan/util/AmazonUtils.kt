package io.github.alessandrojean.toshokan.util

import io.github.alessandrojean.toshokan.service.cover.amazon.AmazonCoverProvider

fun String.toAmazonCoverUrl(): String? = AmazonCoverProvider.isbnToAmazonCoverUrl(this)
