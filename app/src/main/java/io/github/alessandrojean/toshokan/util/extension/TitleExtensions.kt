package io.github.alessandrojean.toshokan.util.extension

data class TitleParts(
  val title: String,
  val number: String? = null,
  val main: String,
  val subtitle: String? = null,
  val full: String
)

private val TITLE_REGEX = "\\s+#([0-9.,]+):?\\s*".toRegex()

fun String.toTitleParts(): TitleParts {
  val match = TITLE_REGEX.find(this)
    ?: return TitleParts(
      title = this,
      main = this,
      full = this
    )

  val title = substring(0, match.groups[0]!!.range.first)
  val number = match.groupValues[1]
  val subtitle = runCatching { substring(match.groups[0]!!.range.last + 1) }
  val main = runCatching { substring(0, match.groups[1]!!.range.last + 1) }

  return TitleParts(
    title = title,
    number = number,
    main = main.getOrDefault("$title #$number"),
    subtitle = subtitle.getOrDefault("").ifBlank { null },
    full = this
  )
}