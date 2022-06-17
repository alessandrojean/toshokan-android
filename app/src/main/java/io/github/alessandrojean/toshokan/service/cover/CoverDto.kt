package io.github.alessandrojean.toshokan.service.cover

import android.net.Uri
import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class SimpleBookInfo(
  val code: String = "",
  val title: String = "",
  val publisher: String = "",
  val initialCovers: List<BookCover> = emptyList()
) : Parcelable, Serializable

@Parcelize
sealed class BookCover : Parcelable, Serializable {
  data class Custom(val uri: Uri) : BookCover()
  open class External(val imageUrl: String) : BookCover()

  class Current(imageUrl: String) : External(imageUrl)
  class Result(
    @StringRes var source: Int? = null,
    imageUrl: String
  ) : External(imageUrl)
}
