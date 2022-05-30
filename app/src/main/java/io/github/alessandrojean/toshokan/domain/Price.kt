package io.github.alessandrojean.toshokan.domain

import android.icu.util.Currency
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class Price(
  val currency: Currency,
  val value: Float
) : Parcelable, Serializable
