package io.github.alessandrojean.toshokan.data.coil

/**
 * Custom fetcher implementation to allow custom covers.
 *
 * Based on Tachiyomi's MangaCoverFetcher implementation.
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

import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.disk.DiskCache
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.key.Keyer
import coil.network.HttpException
import coil.request.Options
import coil.request.Parameters
import io.github.alessandrojean.toshokan.data.cache.CoverCache
import io.github.alessandrojean.toshokan.database.data.Book
import io.github.alessandrojean.toshokan.database.data.CompleteBook
import io.github.alessandrojean.toshokan.domain.DomainBook
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.suspendCancellableCoroutine
import logcat.LogPriority
import logcat.logcat
import okhttp3.CacheControl
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.closeQuietly
import okio.Path.Companion.toOkioPath
import okio.Source
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException
import kotlin.coroutines.resumeWithException

class CoverFetcher<T : Any>(
  private val entity: T,
  private val keyer: Keyer<T>,
  private val options: Options,
  private val coverCache: CoverCache,
  private val callFactoryLazy: Lazy<Call.Factory>,
  private val diskCacheLazy: Lazy<DiskCache>
) : Fetcher {

  private val diskCacheKey: String? by lazy { keyer.key(entity, options) }
  private lateinit var url: String

  override suspend fun fetch(): FetchResult {
    val customCoverFile = when (entity) {
      is Book -> coverCache.getCustomCoverFile(entity)
      is CompleteBook -> coverCache.getCustomCoverFile(entity)
      is DomainBook -> coverCache.getCustomCoverFile(entity)
      else -> error("Invalid entity type")
    }

    if (customCoverFile.exists()) {
      return fileLoader(customCoverFile)
    }

    url = diskCacheKey ?: error("No cover specified")
    return when (getResourceType(url)) {
      Type.URL -> httpLoader()
      Type.File -> fileLoader(File(url.substringAfter("file://")))
      null -> error("Invalid image")
    }
  }

  private fun fileLoader(file: File): FetchResult {
    return SourceResult(
      source = ImageSource(file = file.toOkioPath(), diskCacheKey = diskCacheKey),
      mimeType = "image/*",
      dataSource = DataSource.DISK
    )
  }

  private suspend fun httpLoader(): FetchResult {
    val coverCacheFile = when (entity) {
      is Book -> coverCache.getCoverFile(entity)
      is CompleteBook -> coverCache.getCoverFile(entity)
      is DomainBook -> coverCache.getCoverFile(entity)
      else -> error("Invalid entity type")
    } ?: error("No cover specified")

    if (coverCacheFile.exists() && options.diskCachePolicy.readEnabled) {
      return fileLoader(coverCacheFile)
    }

    var snapshot = readFromDiskCache()
    try {
      if (snapshot != null) {
        val snapshotCoverCache = moveSnapshotToCoverCache(snapshot, coverCacheFile)
        if (snapshotCoverCache != null) {
          return fileLoader(snapshotCoverCache)
        }

        return SourceResult(
          source = snapshot.toImageSource(),
          mimeType = "image/*",
          dataSource = DataSource.DISK
        )
      }

      val response = executeNetworkRequest()
      val responseBody = checkNotNull(response.body) { "Null response source" }
      try {
        val responseCoverCache = writeResponseToCoverCache(response, coverCacheFile)
        if (responseCoverCache != null) {
          return fileLoader(responseCoverCache)
        }

        snapshot = writeToDiskCache(snapshot, response)
        if (snapshot != null) {
          return SourceResult(
            source = snapshot.toImageSource(),
            mimeType = "image/*",
            dataSource = DataSource.NETWORK
          )
        }

        return SourceResult(
          source = ImageSource(source = responseBody.source(), context = options.context),
          mimeType = "image/*",
          dataSource = if (response.cacheResponse != null) DataSource.DISK else DataSource.NETWORK
        )
      } catch (e: Exception) {
        responseBody.closeQuietly()
        throw e
      }
    } catch (e: Exception) {
      snapshot?.closeQuietly()
      throw e
    }
  }

  private suspend fun executeNetworkRequest(): Response {
    val client = callFactoryLazy.value
    val response = client.newCall(newRequest()).await()
    if (!response.isSuccessful && response.code != HttpStatusCode.NotModified.value) {
      response.body?.closeQuietly()
      throw HttpException(response)
    }
    return response
  }

  private fun newRequest(): Request {
    val request = Request.Builder()
      .url(url)
      .headers(options.headers)
      .tag(Parameters::class.java, options.parameters)

    val diskRead = options.diskCachePolicy.readEnabled
    val networkRead = options.networkCachePolicy.readEnabled

    when {
      !networkRead && diskRead -> {
        request.cacheControl(CacheControl.FORCE_CACHE)
      }
      networkRead && !diskRead -> {
        if (options.diskCachePolicy.writeEnabled) {
          request.cacheControl(CacheControl.FORCE_NETWORK)
        } else {
          request.cacheControl(CACHE_CONTROL_FORCE_NETWORK_NO_CACHE)
        }
      }
      !networkRead && !diskRead -> {
        request.cacheControl(CACHE_CONTROL_NO_NETWORK_NO_CACHE)
      }
    }

    return request.build()
  }

  private fun moveSnapshotToCoverCache(snapshot: DiskCache.Snapshot, cacheFile: File?): File? {
    if (cacheFile == null) {
      return null
    }

    return try {
      diskCacheLazy.value.run {
        fileSystem.source(snapshot.data).use { input ->
          writeSourceToCoverCache(input, cacheFile)
        }
        remove(diskCacheKey!!)
      }
      cacheFile.takeIf { it.exists() }
    } catch (e: Exception) {
      logcat(LogPriority.ERROR) { "Failed to write snapshot data to cover cache ${cacheFile.name}" }
      null
    }
  }

  private fun writeResponseToCoverCache(response: Response, cacheFile: File?): File? {
    if (cacheFile == null || !options.diskCachePolicy.writeEnabled) {
      return null
    }

    return try {
      response.peekBody(Long.MAX_VALUE).source().use { input ->
        writeSourceToCoverCache(input, cacheFile)
      }
      cacheFile.takeIf { it.exists() }
    } catch (e: Exception) {
      logcat(LogPriority.ERROR) { "Failed to write response data to cover cache ${cacheFile.name}" }
      null
    }
  }

  private fun writeSourceToCoverCache(input: Source, cacheFile: File) {
    cacheFile.parentFile?.mkdirs()
    cacheFile.delete()
    try {
      cacheFile.sink().buffer().use { output ->
        output.writeAll(input)
      }
    } catch (e: Exception) {
      cacheFile.delete()
      throw e
    }
  }

  private fun readFromDiskCache(): DiskCache.Snapshot? {
    return if (options.diskCachePolicy.readEnabled) diskCacheLazy.value[diskCacheKey!!] else null
  }

  private fun writeToDiskCache(
    snapshot: DiskCache.Snapshot?,
    response: Response
  ): DiskCache.Snapshot? {
    if (!options.diskCachePolicy.writeEnabled) {
      snapshot?.closeQuietly()
      return null
    }

    val editor = if (snapshot != null) {
      snapshot.closeAndEdit()
    } else {
      diskCacheLazy.value.edit(diskCacheKey!!)
    } ?: return null

    try {
      diskCacheLazy.value.fileSystem.write(editor.data) {
        response.body!!.source().readAll(this)
      }
      return editor.commitAndGet()
    } catch (e: Exception) {
      try {
        editor.abort()
      } catch (ignored: Exception) {}

      throw e
    }
  }

  private fun DiskCache.Snapshot.toImageSource(): ImageSource {
    return ImageSource(file = data, diskCacheKey = diskCacheKey, closeable = this)
  }

  private fun getResourceType(cover: String?): Type? {
    return when {
      cover.isNullOrEmpty() -> null
      cover.startsWith("http", true) || cover.startsWith("Custom-", true) -> Type.URL
      cover.startsWith("/") || cover.startsWith("file://") -> Type.File
      else -> null
    }
  }

  // Based on https://github.com/gildor/kotlin-coroutines-okhttp
  private suspend fun Call.await(): Response {
    return suspendCancellableCoroutine { continuation ->
      enqueue(
        object : Callback {
          override fun onResponse(call: Call, response: Response) {
            if (!response.isSuccessful) {
              continuation.resumeWithException(IllegalStateException("HTTP error ${response.code}"))
              return
            }

            continuation.resume(response) {
              response.body?.closeQuietly()
            }
          }

          override fun onFailure(call: Call, e: IOException) {
            // Don't bother with resuming the continuation if it is already cancelled.
            if (continuation.isCancelled) return
            continuation.resumeWithException(e)
          }
        },
      )

      continuation.invokeOnCancellation {
        try {
          cancel()
        } catch (ex: Throwable) {
          // Ignore cancel exception
        }
      }
    }
  }

  private enum class Type {
    File, URL
  }

  class Factory<T: Any>(
    private val coverCache: CoverCache,
    private val keyer: Keyer<T>,
    private val callFactoryLazy: Lazy<Call.Factory>,
    private val diskCacheLazy: Lazy<DiskCache>
  ) : Fetcher.Factory<T> {

    override fun create(data: T, options: Options, imageLoader: ImageLoader): Fetcher {
      return CoverFetcher(data, keyer, options, coverCache, callFactoryLazy, diskCacheLazy)
    }

  }

  companion object {
    private val CACHE_CONTROL_FORCE_NETWORK_NO_CACHE = CacheControl.Builder().noCache().noStore().build()
    private val CACHE_CONTROL_NO_NETWORK_NO_CACHE = CacheControl.Builder().noCache().onlyIfCached().build()
  }

}