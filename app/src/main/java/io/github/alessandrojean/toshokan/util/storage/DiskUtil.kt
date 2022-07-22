package io.github.alessandrojean.toshokan.util.storage

import io.github.alessandrojean.toshokan.util.extension.md5

object DiskUtil {

  fun hashKeyForDisk(key: String): String = key.md5()

}