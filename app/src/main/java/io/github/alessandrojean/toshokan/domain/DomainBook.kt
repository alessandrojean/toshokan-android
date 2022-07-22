package io.github.alessandrojean.toshokan.domain

import android.icu.util.Currency
import android.os.Parcelable
import io.github.alessandrojean.toshokan.data.backup.models.ToshokanSheetBook
import io.github.alessandrojean.toshokan.data.backup.models.ToshokanSheetDimensions
import io.github.alessandrojean.toshokan.data.backup.models.ToshokanSheetPrice
import io.github.alessandrojean.toshokan.data.backup.models.ToshokanSheetStatus
import io.github.alessandrojean.toshokan.util.extension.toIsoStringToSheet
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.time.format.DateTimeFormatter

@Parcelize
data class DomainBook(
  val id: Long? = null,
  val code: String? = null,
  val group: DomainRelation,
  val title: String,
  val contributors: List<DomainContributor> = emptyList(),
  val publisher: DomainRelation,
  val dimensions: DomainDimensions,
  val isFuture: Boolean = false,
  val labelPrice: DomainPrice,
  val paidPrice: DomainPrice,
  val store: DomainRelation,
  val coverUrl: String? = null,
  val boughtAt: Long? = null,
  val isFavorite: Boolean = false,
  val synopsis: String? = null,
  val notes: String? = null,
  val tags: List<DomainTag> = emptyList(),
  val readings: List<DomainReading> = emptyList(),
  val readingCount: Long? = null,
  val latestReading: Long? = null,
  val pageCount: Int? = null,
  val createdAt: Long,
  val updatedAt: Long,
) : Parcelable, Serializable {

  fun toSheetBook(): ToshokanSheetBook = ToshokanSheetBook(
    code = code.orEmpty(),
    group = group.title.orEmpty(),
    title = title,
    authors = contributors.mapNotNull { it.name },
    publisher = publisher.title.orEmpty(),
    dimensions = dimensions.toSheetDimensions(),
    status = when {
      isFuture -> ToshokanSheetStatus.FUTURE
      (readingCount ?: 0) > 0 -> ToshokanSheetStatus.READ
      else -> ToshokanSheetStatus.UNREAD
    },
    readAt = readings.firstOrNull()?.readAt?.toIsoStringToSheet(),
    labelPrice = labelPrice.toSheetPrice(),
    paidPrice = paidPrice.toSheetPrice(),
    store = store.title.orEmpty(),
    coverUrl = coverUrl,
    boughtAt = boughtAt?.toIsoStringToSheet(),
    favorite = isFavorite,
    synopsis = synopsis,
    notes = notes,
    tags = tags.mapNotNull { it.title },
    createdAt = createdAt.toIsoStringToSheet(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    updatedAt = updatedAt.toIsoStringToSheet(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
  )

}

@Parcelize
data class DomainRelation(
  val id: Long? = null,
  val title: String? = null
) : Parcelable, Serializable

@Parcelize
data class DomainContributor(
  val id: Long? = null,
  val personId: Long? = null,
  val name: String? = null,
  val role: CreditRole = CreditRole.AUTHOR
) : Parcelable, Serializable {

  fun toContributor(): Contributor = Contributor(
    personId = personId,
    personText = name.orEmpty(),
    role = role
  )

}

@Parcelize
data class DomainTag(
  val id: Long? = null,
  val title: String? = null,
  val isNsfw: Boolean = false
) : Parcelable, Serializable {

  fun toRawTag(): RawTag = RawTag(
    tagId = id,
    tagText = title.orEmpty()
  )

}


@Parcelize
data class DomainDimensions(
  val width: Float,
  val height: Float
) : Parcelable, Serializable {

  fun toSheetDimensions(): ToshokanSheetDimensions = ToshokanSheetDimensions(
    width = width,
    height = height
  )

}

@Parcelize
data class DomainPrice(
  val currency: String,
  val value: Float
) : Parcelable, Serializable {

  fun toPrice(): Price = Price(
    currency = Currency.getInstance(currency),
    value = value
  )

  fun toSheetPrice(): ToshokanSheetPrice = ToshokanSheetPrice(
    currency = currency,
    value = value
  )

}

@Parcelize
data class DomainReading(
  val id: Long? = null,
  val readAt: Long? = null
) : Parcelable, Serializable