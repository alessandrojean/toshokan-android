package io.github.alessandrojean.toshokan.data.backup.models

import android.icu.util.Currency
import android.os.Parcelable
import io.github.alessandrojean.toshokan.domain.DomainBook
import io.github.alessandrojean.toshokan.domain.DomainContributor
import io.github.alessandrojean.toshokan.domain.DomainDimensions
import io.github.alessandrojean.toshokan.domain.DomainPrice
import io.github.alessandrojean.toshokan.domain.DomainReading
import io.github.alessandrojean.toshokan.domain.DomainRelation
import io.github.alessandrojean.toshokan.domain.DomainTag
import io.github.alessandrojean.toshokan.domain.Price
import io.github.alessandrojean.toshokan.util.extension.toUtcEpochMilliFromSheet
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import java.time.format.DateTimeFormatter

@Serializable
data class ToshokanSheet(
  @ProtoNumber(1) val version: Int,
  @ProtoNumber(2) val sheetVersion: Int,
  @ProtoNumber(3) val groups: List<String>,
  @ProtoNumber(4) val tags: List<String>,
  @ProtoNumber(5) val publishers: List<String>,
  @ProtoNumber(6) val stores: List<String>,
  @ProtoNumber(7) val authors: List<String>,
  @ProtoNumber(8) val library: List<ToshokanSheetBook>,
  @ProtoNumber(9) val owner: ToshokanOwner? = null
)

@Serializable
@Parcelize
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
  @ProtoNumber(19) val updatedAt: String,
  @ProtoNumber(20) val sheetVersion: Int? = null,
  @ProtoNumber(21) val owner: ToshokanOwner? = null
) : Parcelable, java.io.Serializable {

  fun toDomainBook(): DomainBook = DomainBook(
    code = code,
    group = DomainRelation(title = group),
    title = title,
    contributors = authors.map { DomainContributor(name = it) },
    publisher = DomainRelation(title = publisher),
    dimensions = dimensions.toDomainDimensions(),
    isFuture = status == ToshokanSheetStatus.FUTURE,
    readings = listOfNotNull(
      DomainReading(readAt = readAt?.toUtcEpochMilliFromSheet())
        .takeIf { status == ToshokanSheetStatus.READ }
    ),
    readingCount = if (status == ToshokanSheetStatus.READ) 1 else 0,
    labelPrice = labelPrice.toDomainPrice(),
    paidPrice = paidPrice.toDomainPrice(),
    store = DomainRelation(title = store),
    coverUrl = coverUrl,
    boughtAt = boughtAt?.toUtcEpochMilliFromSheet(),
    isFavorite = favorite,
    synopsis = synopsis,
    notes = notes,
    tags = tags.map { DomainTag(title = it) },
    createdAt = createdAt.toUtcEpochMilliFromSheet(DateTimeFormatter.ISO_LOCAL_DATE_TIME)!!,
    updatedAt = updatedAt.toUtcEpochMilliFromSheet(DateTimeFormatter.ISO_LOCAL_DATE_TIME)!!
  )

}

@Serializable
@Parcelize
data class ToshokanSheetDimensions(
  @ProtoNumber(1) val width: Float,
  @ProtoNumber(2) val height: Float
) : Parcelable, java.io.Serializable {

  fun toDomainDimensions(): DomainDimensions = DomainDimensions(
    width = width,
    height = height
  )

}

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
@Parcelize
data class ToshokanSheetPrice(
  @ProtoNumber(1) val currency: String,
  @ProtoNumber(2) val value: Float
) : Parcelable, java.io.Serializable {

  fun toPrice(): Price = Price(
    currency = Currency.getInstance(currency),
    value = value
  )

  fun toDomainPrice(): DomainPrice = DomainPrice(
    currency = currency,
    value = value
  )

}

@Serializable
@Parcelize
data class ToshokanOwner(
  @ProtoNumber(1) val name: String,
  @ProtoNumber(2) val pictureUrl: String
) : Parcelable, java.io.Serializable