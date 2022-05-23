package io.github.alessandrojean.toshokan.service.lookup

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LookupBookResult(
  val provider: Provider? = null,
  val providerId: String = "",
  val isbn: String = "",
  val title: String = "",
  val authors: List<String> = emptyList(),
  val publisher: String = "",
  val synopsis: String = "",
  val dimensions: List<Float> = emptyList(),
  val coverUrl: String = ""
) : Parcelable
