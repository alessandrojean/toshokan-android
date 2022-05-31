package io.github.alessandrojean.toshokan.util.extension

data class TitleParts(
  val title: String,
  val number: String? = null,
  val main: String,
  val subtitle: String? = null
)

private val TITLE_REGEX = "\\s+#(\\d+(?:[.,]\\d+)?)(?::\\s+)?".toRegex()

fun String.toTitleParts(): TitleParts {
  val parts = split(TITLE_REGEX)
  val main = parts.getOrNull(2).let {
    if (it == null) {
      this
    } else {
      substring(0, indexOf(parts[2]) - 2).trim()
    }
  }

  return TitleParts(
    title = parts[0].trim(),
    number = parts.getOrNull(1),
    main = main,
    subtitle = if (!parts.getOrNull(2).isNullOrBlank()) {
      replace(main, "")
        .replace(":", "")
        .trim()
    } else {
      null
    }
  )
}