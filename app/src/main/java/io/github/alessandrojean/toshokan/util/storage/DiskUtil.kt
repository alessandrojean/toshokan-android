package io.github.alessandrojean.toshokan.util.storage

import java.security.MessageDigest

object DiskUtil {

  fun hashKeyForDisk(key: String): String {
    return MessageDigest.getInstance("MD5")
      .digest(key.toByteArray())
      .joinToString("") { byte -> "%02x".format(byte) }
  }

}