package io.github.alessandrojean.toshokan.util

private val ISBN_REGEX = "^[0-9]{13}$|^[0-9]{9}[0-9xX]$".toRegex()

fun String.isValidIsbn(): Boolean {
  val onlyDigits = replace("-", "")

  if (onlyDigits.length != 10 && onlyDigits.length != 13) {
    return false
  }

  if (!onlyDigits.matches(ISBN_REGEX)) {
    return false
  }

  if (length == 10) {
    val parcels = onlyDigits.mapIndexed { i, digit ->
      (10 - i) * (if (digit == 'X' || digit == 'x') 10 else digit.digitToInt(10))
    }

    return parcels.sum() % 11 == 0
  }

  val parcels = onlyDigits.mapIndexed { i, digit ->
    digit.digitToInt(10) * (if ((i + 1) % 2 == 0) 3 else 1)
  }

  return parcels.sum() % 10 == 0
}

fun String.toIsbn10(): String? {
  if (!isValidIsbn()) {
    return null
  }

  val onlyDigits = replace("-", "")

  if (onlyDigits.length == 10) {
    return onlyDigits
  }

  val equalPart = substring(3).dropLast(1)
  val sum = equalPart
    .mapIndexed { i, digit -> digit.digitToInt(10) * (i + 1) }
    .sum()
  val lastDigit = sum % 11

  return equalPart + (if (lastDigit == 10) "X" else lastDigit.toString())
}