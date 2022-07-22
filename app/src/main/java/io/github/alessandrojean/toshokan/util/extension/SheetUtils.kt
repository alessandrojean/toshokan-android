package io.github.alessandrojean.toshokan.util.extension

import android.net.Uri
import com.google.mlkit.vision.barcode.common.Barcode
import io.github.alessandrojean.toshokan.data.backup.models.ToshokanSheetBook
import io.github.alessandrojean.toshokan.domain.DomainBook
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import logcat.LogPriority
import logcat.logcat
import okio.Buffer
import okio.Okio
import okio.buffer
import okio.gzip
import okio.sink
import okio.source
import java.io.ByteArrayOutputStream

object SheetUtils {

  private const val TOSHOKAN_WEB_SHARE_URL = "https://toshokan-app.netlify.app/share"

  fun createBookShareUrl(book: DomainBook): Uri {
    val sheetBook = book.toSheetBook()
    val encodedBook = ByteArrayOutputStream().use { outStream ->
      ProtoBuf.encodeToByteArray(sheetBook).inputStream().source().buffer().use { source ->
        outStream.sink().gzip().buffer().use { it.writeAll(source) }
      }

      outStream.toByteArray().encodeBase64()
    }

    return Uri.parse(TOSHOKAN_WEB_SHARE_URL).buildUpon()
      .appendQueryParameter("d", encodedBook)
      .build()
  }

  fun decodeBook(barcode: Barcode): ToshokanSheetBook? {
    val book = runCatching {
      barcode.rawBytes?.inputStream()?.source()?.gzip()?.buffer()
        ?.use { it.readByteArray() }
        ?.let { ProtoBuf.decodeFromByteArray<ToshokanSheetBook>(it) }
    }

    book.exceptionOrNull()?.let {
      logcat(LogPriority.ERROR) { it.stackTraceToString() }
    }

    return book.getOrNull()
  }

  fun decodeBook(base64Gzipped: String): ToshokanSheetBook? {
    val book = runCatching {
      base64Gzipped.decodeBase64().inputStream()
        .source()
        .gzip()
        .buffer()
        .use { it.readByteArray() }
        .let { ProtoBuf.decodeFromByteArray<ToshokanSheetBook>(it) }
    }

    return book.getOrNull()
  }

}