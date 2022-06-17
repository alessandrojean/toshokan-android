package io.github.alessandrojean.toshokan.data

import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import io.github.alessandrojean.toshokan.util.extension.cacheImagesDir
import io.github.alessandrojean.toshokan.util.extension.getUriCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import logcat.LogPriority
import logcat.logcat
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageSaver @Inject constructor(
  private val application: Application
) {

  data class Image(
    val bitmap: Bitmap,
    val fileName: String = UUID.randomUUID().toString(),
    val location: Location
  )

  sealed class Location {
    object Cache : Location()
    object Downloads : Location()
  }

  sealed class Result {
    data class Success(val uri: Uri?) : Result()
    data class Failure(val error: Throwable) : Result()
  }

  suspend fun saveImage(image: Image): Result = withContext(Dispatchers.IO) {
    try {
      val savedUri = when (image.location) {
        is Location.Cache -> saveImageInCache(image)
        is Location.Downloads -> saveImageInDownloads(image)
      }

      Result.Success(uri = savedUri)
    } catch (e: Throwable) {
      logcat(LogPriority.ERROR) { e.stackTraceToString() }
      Result.Failure(e)
    }
  }

  private fun saveImageInCache(image: Image): Uri {
    val locationFolder = application.cacheImagesDir

    val destFile = File(locationFolder, "${image.fileName}.jpg")
    val byteArrayInputStream = ByteArrayOutputStream().run {
      image.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, this)
      ByteArrayInputStream(this.toByteArray())
    }

    byteArrayInputStream.use { input ->
      destFile.outputStream().use { output ->
        input.copyTo(output)
      }
    }

    return destFile.getUriCompat(application)
  }

  private fun saveImageInDownloads(image: Image): Uri? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      val values = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, "${image.fileName}.jpg")
        put(MediaStore.Downloads.MIME_TYPE, "image/jpeg")
        put(MediaStore.Downloads.IS_PENDING, true)
      }

      val uri = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
      val itemUri = application.contentResolver.insert(uri, values)

      itemUri?.also {
        application.contentResolver.openOutputStream(itemUri).use { output ->
          image.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
        }

        values.put(MediaStore.Images.Media.IS_PENDING, false)
        application.contentResolver.update(itemUri, values, null, null)
      }
    } else {
      val downloadsDirectory = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
      )

      downloadsDirectory.also {
        if (!it.exists()) {
          it.mkdir()
        }
      }

      val destFile = File(downloadsDirectory, "${image.fileName}.jpg")
      val byteArrayInputStream = ByteArrayOutputStream().run {
        image.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, this)
        ByteArrayInputStream(this.toByteArray())
      }

      byteArrayInputStream.use { input ->
        destFile.outputStream().use { output ->
          input.copyTo(output)
        }
      }

      return destFile.getUriCompat(application)
    }
  }

}