package io.github.alessandrojean.toshokan.util.extension

import java.text.Normalizer
import java.util.Locale

fun String.toSlug(locale: Locale = Locale.getDefault()): String {
  return Normalizer
    .normalize(this, Normalizer.Form.NFD)
    .replace(NON_ASCII_REGEX, "")
    .replace(NON_NUMBERS_OR_DIGITS, "")
    .replace(MULTIPLE_SPACES_REGEX, "-")
    .lowercase(locale)
}

private val NON_ASCII_REGEX = "[^\\p{ASCII}]".toRegex()
private val NON_NUMBERS_OR_DIGITS = "[^a-zA-Z0-9\\s]+".toRegex()
private val MULTIPLE_SPACES_REGEX = "\\s+".toRegex()
