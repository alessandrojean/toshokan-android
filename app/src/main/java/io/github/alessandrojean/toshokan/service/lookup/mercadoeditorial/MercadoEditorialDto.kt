package io.github.alessandrojean.toshokan.service.lookup.mercadoeditorial

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MercadoEditorialResult(
  val books: List<MercadoEditorialBook>? = emptyList(),
  val status: MercadoEditorialStatus,
)

@Serializable
data class MercadoEditorialStatus(
  val success: Boolean = true,
  val code: Int = 200,
)

@Serializable
data class MercadoEditorialBook(
  val isbn: String = "",
  @SerialName("titulo") val title: String = "",
  @SerialName("subtitulo") val subtitle: String = "",
  @SerialName("contribuicao") val contributions: List<MercadoEditorialContribution>? = emptyList(),
  @SerialName("sinopse") val synopsis: String? = "",
  @SerialName("medidas") val dimensions: MercadoEditorialDimension? = null,
  @SerialName("imagens") val pictures: MercadoEditorialPictures? = null,
  @SerialName("editora") val publisher: MercadoEditorialPublisher? = null,
)

@Serializable
data class MercadoEditorialContribution(
  @SerialName("nome") val firstName: String = "",
  @SerialName("sobrenome") val lastName: String = "",
  @SerialName("codigo_contribuicao") val code: Int,
)

@Serializable
data class MercadoEditorialDimension(
  @SerialName("altura") val height: String = "",
  @SerialName("largura") val width: String = ""
)

@Serializable
data class MercadoEditorialPictures(
  @SerialName("imagem_primeira_capa") val cover: MercadoEditorialImage? = null
)

@Serializable
data class MercadoEditorialImage(
  @SerialName("pequena") val small: String = "",
  @SerialName("media") val medium: String = "",
  @SerialName("grande") val large: String = "",
)

@Serializable
data class MercadoEditorialPublisher(
  @SerialName("nome_fantasia") val name: String = "",
)
