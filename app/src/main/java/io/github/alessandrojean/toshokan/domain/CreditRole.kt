package io.github.alessandrojean.toshokan.domain

import androidx.annotation.StringRes
import io.github.alessandrojean.toshokan.R

enum class CreditRole(val code: Long, @StringRes val title: Int) {
  UNKNOWN(0, R.string.role_unknown),
  AUTHOR(1, R.string.role_author),
  ARTIST(2, R.string.role_artist),
  ILLUSTRATOR(3, R.string.role_illustrator),
  SCRIPT(4, R.string.role_script),
  EDITOR(5, R.string.role_editor),
  TRANSLATOR(6, R.string.role_translator),
  COVER_DESIGN(7, R.string.role_cover_design),
  COLORIST(8, R.string.role_colorist),
  LETTERER(9, R.string.role_letterer),
  GRAPHIC_DESIGN(10, R.string.role_graphic_design),
  REVIEWER(11, R.string.role_reviewer),
  PRINTING_COMPANY(12, R.string.role_printing_company),
  OTHERS(13, R.string.role_others)
}