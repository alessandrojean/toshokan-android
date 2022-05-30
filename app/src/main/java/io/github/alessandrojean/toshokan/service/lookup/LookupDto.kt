package io.github.alessandrojean.toshokan.service.lookup

import android.os.Parcelable
import io.github.alessandrojean.toshokan.domain.CreditRole
import io.github.alessandrojean.toshokan.domain.Price
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class LookupBookResult(
  val provider: Provider? = null,
  val providerId: String = "",
  val isbn: String = "",
  val title: String = "",
  val contributors: List<LookupBookContributor> = emptyList(),
  val publisher: String = "",
  val synopsis: String = "",
  val dimensions: List<Float> = emptyList(),
  val labelPrice: Price? = null,
  val coverUrl: String = ""
) : Parcelable, Serializable

@Parcelize
data class LookupBookContributor(
  val name: String,
  val role: CreditRole
) : Parcelable, Serializable

