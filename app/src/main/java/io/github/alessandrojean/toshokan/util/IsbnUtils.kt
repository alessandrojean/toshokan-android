package io.github.alessandrojean.toshokan.util

val ISBN_REGEX = "^[0-9]{13}$|^[0-9]{9}[0-9xX]$".toRegex()
val ISSN_REGEX = "^[0-9]{7}[0-9xX]$".toRegex()
val EAN_REGEX = "^[0-9]{13}$".toRegex()

fun String.removeDashes(): String {
  return replace("-", "")
}

fun String.isValidBarcode(): Boolean {
  return isValidIsbn() || isValidIssn() || isValidEan()
}

fun String.isValidIsbn(): Boolean {
  val onlyDigits = removeDashes()

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

fun String.isValidIssn(): Boolean {
  val onlyDigits = removeDashes()

  if (!onlyDigits.matches(ISSN_REGEX)) {
    return false
  }

  val calcDigit = onlyDigits
    .dropLast(1)
    .mapIndexed { index, c -> (8 - index) * c.digitToInt(10) }
    .sum()
    .let { it % 11 }
    .let { if (it != 0) 11 - it else it }
    .let { if (it == 10) 'X' else it.digitToChar(10) }

  return calcDigit == onlyDigits.last()
}

fun String.isValidEan(): Boolean {
  val onlyDigits = removeDashes()

  if (!onlyDigits.matches(EAN_REGEX)) {
    return false
  }

  val checkSum = onlyDigits
    .dropLast(1)
    .mapIndexed { index, c -> c.digitToInt(10) * (if ((index + 1) % 2 == 0) 3 else 1) }
    .sum()

  return (10 - (checkSum % 10)) % 10 == onlyDigits.last().digitToInt(10)
}

fun String.toIsbn10(): String? {
  if (!isValidIsbn()) {
    return null
  }

  val onlyDigits = removeDashes()

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

data class IsbnInformation(
  val group: Int,
  val country: String,
  val language: String
)

val RegistrationGroups = arrayOf(
  IsbnInformation(0, "US", "en"),
  IsbnInformation(1, "US", "en"),
  IsbnInformation(2, "FR", "fr"),
  IsbnInformation(3, "DE", "de"),
  IsbnInformation(4, "JP", "ja"),
  IsbnInformation(5, "RU", "ru"),
  IsbnInformation(7, "CN", "zh"),
  IsbnInformation(65, "BR", "pt-BR"),
  IsbnInformation(84, "ES", "es"),
  IsbnInformation(85, "BR", "pt-BR"),
  IsbnInformation(88, "IT", "it"),
  IsbnInformation(89, "KR", "ko"),
  IsbnInformation(607, "MX", "es"),
  IsbnInformation(612, "PE", "es"),
  IsbnInformation(950, "AR", "es"),
  IsbnInformation(956, "CL", "es"),
  IsbnInformation(958, "CO", "es"),
  IsbnInformation(968, "MX", "es"),
  IsbnInformation(970, "MX", "es"),
  IsbnInformation(972, "PT", "pt"),
  IsbnInformation(987, "AR", "es"),
  IsbnInformation(989, "PT", "pt"),
  IsbnInformation(9915, "UY", "es"),
  IsbnInformation(9917, "BO", "es"),
  IsbnInformation(9946, "KP", "ko"),
  IsbnInformation(9972, "PE", "es"),
  IsbnInformation(9974, "UY", "es"),
  IsbnInformation(99905, "BO", "es"),
  IsbnInformation(99954, "BO", "es"),
  IsbnInformation(99974, "BO", "es")
)

fun String.toIsbnInformation(): IsbnInformation? {
  val onlyDigits = removeDashes()

  if (!onlyDigits.isValidIsbn()) {
    return null
  }

  val usefulPart = if (onlyDigits.length == 13) {
    onlyDigits.substring(3)
  } else {
    onlyDigits
  }

  for (i in (1..5)) {
    val group = usefulPart.substring(0, i)
    val info = RegistrationGroups.firstOrNull { it.group.toString() == group }

    if (info != null) {
      return info
    }
  }

  return null
}
