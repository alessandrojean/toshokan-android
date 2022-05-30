package io.github.alessandrojean.toshokan.service.lookup.cbl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CblSearchRequest(
  val count: Boolean,
  val facets: List<String>,
  val filter: String,
  @SerialName("orderby") val orderBy: String?,
  val queryType: String,
  val search: String,
  val searchFields: String,
  val searchMode: String,
  val select: String,
  val skip: Int,
  val top: Int
)

@Serializable
data class CblSearchResult(
  @SerialName("@odata.count") val count: Int = 0,
  val value: List<CblRecord> = emptyList()
)

@Serializable
data class CblRecord(
  @SerialName("RecordId") val id: String? = "",
  @SerialName("Authors") val authors: List<String> = emptyList(),
  @SerialName("Dimensao") val dimensions: String? = "",
  @SerialName("Imprint") val publisher: String = "",
  @SerialName("Profissoes") val roles: List<String>? = emptyList(),
  @SerialName("RowKey") val rowKey: String = "",
  @SerialName("Sinopse") val synopsis: String? = "",
  @SerialName("Title") val title: String = ""
)