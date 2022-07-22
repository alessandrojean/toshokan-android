package io.github.alessandrojean.toshokan.data.cache

/**
 * Custom cover cache implementation to allow custom covers.
 *
 * Based on Tachiyomi's CoverCache implementation.
 * https://github.com/tachiyomiorg/tachiyomi
 *
 * Copyright 2015 Javier Tomás
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */

import android.content.Context
import coil.imageLoader
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.alessandrojean.toshokan.database.data.Book
import io.github.alessandrojean.toshokan.database.data.CompleteBook
import io.github.alessandrojean.toshokan.domain.DomainBook
import io.github.alessandrojean.toshokan.util.storage.DiskUtil
import java.io.File
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoverCache @Inject constructor(@ApplicationContext private val context: Context) {

  companion object {
    private const val COVERS_DIR = "covers"
    private const val CUSTOM_COVERS_DIR = "covers/custom"
  }

  private val cacheDir = getCacheDir(COVERS_DIR)
  private val customCoverCacheDir = getCacheDir(CUSTOM_COVERS_DIR)

  fun getCoverFile(book: Book): File? {
    return getCoverFile(book.cover_url)
  }

  fun getCoverFile(book: CompleteBook): File? {
    return getCoverFile(book.cover_url)
  }

  fun getCoverFile(book: DomainBook): File? {
    return getCoverFile(book.coverUrl)
  }

  fun getCoverFile(coverUrl: String?): File? {
    return coverUrl?.let {
      File(cacheDir, DiskUtil.hashKeyForDisk(it))
    }
  }

  fun getCustomCoverFile(book: DomainBook): File {
    return if (book.id != null) {
      getCustomCoverFile(book.id.toString())
    } else {
      getCustomCoverFile(book.toString())
    }
  }

  fun getCustomCoverFile(book: Book): File = getCustomCoverFile(book.id.toString())

  fun getCustomCoverFile(book: CompleteBook): File = getCustomCoverFile(book.id.toString())

  fun getCustomCoverFile(key: String): File {
    return File(customCoverCacheDir, DiskUtil.hashKeyForDisk(key))
  }

  @Throws(IOException::class)
  fun setCustomCoverToCache(book: Book, inputStream: InputStream) {
    return setCustomCoverToCache(book.id, inputStream)
  }

  @Throws(IOException::class)
  fun setCustomCoverToCache(bookId: Long, inputStream: InputStream) {
    getCustomCoverFile(bookId.toString()).outputStream().use { output ->
      inputStream.copyTo(output)
    }
  }

  fun deleteFromCache(book: Book, deleteCustomCover: Boolean = false): Int {
    return deleteFromCache(book.id, book.cover_url, deleteCustomCover)
  }

  fun deleteFromCache(id: Long, coverUrl: String?, deleteCustomCover: Boolean = false): Int {
    var deleted = 0

    getCoverFile(coverUrl)?.let {
      if (it.exists() && it.delete()) ++deleted
    }

    if (deleteCustomCover) {
      if (deleteCustomCover(id)) ++deleted
    }

    return deleted
  }

  fun deleteCustomCover(book: Book): Boolean = deleteCustomCover(book.id)

  fun deleteCustomCover(bookId: Long): Boolean {
    return getCustomCoverFile(bookId.toString()).let {
      it.exists() && it.delete()
    }
  }

  fun clearMemoryCache() {
    context.imageLoader.memoryCache?.clear()
  }

  private fun getCacheDir(dir: String): File {
    return context.getExternalFilesDir(dir)
      ?: File(context.filesDir, dir).also { it.mkdirs() }
  }

}