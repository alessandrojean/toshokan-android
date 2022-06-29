package io.github.alessandrojean.toshokan.data.backup.models

import android.icu.util.Currency
import io.github.alessandrojean.toshokan.domain.Price
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
data class ToshokanSheet(
  @ProtoNumber(1) val version: Int,
  @ProtoNumber(2) val sheetVersion: Int,
  @ProtoNumber(3) val groups: List<String>,
  @ProtoNumber(4) val tags: List<String>,
  @ProtoNumber(5) val publishers: List<String>,
  @ProtoNumber(6) val stores: List<String>,
  @ProtoNumber(7) val authors: List<String>,
  @ProtoNumber(8) val library: List<ToshokanSheetBook>
)

@Serializable
data class ToshokanSheetBook(
  @ProtoNumber(1) val code: String,
  @ProtoNumber(2) val group: String,
  @ProtoNumber(3) val title: String,
  @ProtoNumber(4) val authors: List<String>,
  @ProtoNumber(5) val publisher: String,
  @ProtoNumber(6) val dimensions: ToshokanSheetDimensions,
  @ProtoNumber(7) val status: ToshokanSheetStatus = ToshokanSheetStatus.UNREAD,
  @ProtoNumber(8) val readAt: String? = null,
  @ProtoNumber(9) val labelPrice: ToshokanSheetPrice,
  @ProtoNumber(10) val paidPrice: ToshokanSheetPrice,
  @ProtoNumber(11) val store: String,
  @ProtoNumber(12) val coverUrl: String? = null,
  @ProtoNumber(13) val boughtAt: String? = null,
  @ProtoNumber(14) val favorite: Boolean = false,
  @ProtoNumber(15) val synopsis: String? = null,
  @ProtoNumber(16) val notes: String? = null,
  @ProtoNumber(17) val tags: List<String>,
  @ProtoNumber(18) val createdAt: String,
  @ProtoNumber(19) val updatedAt: String
)

@Serializable
data class ToshokanSheetDimensions(
  @ProtoNumber(1) val width: Float,
  @ProtoNumber(2) val height: Float
)

@Serializable
enum class ToshokanSheetStatus {
  @ProtoNumber(0)
  UNREAD,

  @ProtoNumber(1)
  READ,

  @ProtoNumber(2)
  FUTURE
}

@Serializable
data class ToshokanSheetPrice(
  @ProtoNumber(1) val currency: String,
  @ProtoNumber(2) val value: Float
) {

  fun toPrice(): Price = Price(
    currency = Currency.getInstance(currency),
    value = value
  )

}