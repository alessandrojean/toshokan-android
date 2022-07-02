package io.github.alessandrojean.toshokan.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class RankingItem(
  val itemId: Long?,
  val title: String,
  val count: Long,
  val extra: String? = null
): Parcelable, Serializable
