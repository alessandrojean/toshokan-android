package io.github.alessandrojean.toshokan.util.extension

import android.util.Base64
import java.security.MessageDigest

fun String.md5(): String {
  return MessageDigest.getInstance("MD5")
    .digest(toByteArray())
    .joinToString("") { byte -> "%02x".format(byte) }
}

fun String.decodeBase64(): ByteArray {
  return Base64.decode(this, Base64.DEFAULT)
}

fun ByteArray.encodeBase64(): String {
  return Base64.encodeToString(this, Base64.DEFAULT)
}
