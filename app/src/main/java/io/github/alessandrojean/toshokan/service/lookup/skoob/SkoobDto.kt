package io.github.alessandrojean.toshokan.service.lookup.skoob

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SkoobResponse<T>(
  val success: Boolean = false,
  val error: String? = "",
  val response: T? = null
)

@Serializable
data class SkoobBook(
  @SerialName("livro_id") val bookId: Int,
  @SerialName("titulo") val title: String? = "",
  @SerialName("autor") val author: String? = "",
  val isbn: Long? = 0L,
  @SerialName("editora") val publisher: String? = "",
  @SerialName("sinopse") val synopsis: String? = "",
  @SerialName("capa_grande") val coverUrl: String? = "",
)