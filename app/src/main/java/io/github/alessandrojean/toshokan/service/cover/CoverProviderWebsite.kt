package io.github.alessandrojean.toshokan.service.cover

import androidx.annotation.StringRes
import io.github.alessandrojean.toshokan.R

enum class CoverProviderWebsite(@StringRes val title: Int) {
  AMAZON(R.string.amazon),
  LOJA_PANINI(R.string.loja_panini),
  JBC(R.string.jbc),
  NEWPOP(R.string.newpop),
  PIPOCA_E_NANQUIM(R.string.pipoca_e_nanquim),
  SHUEISHA(R.string.shueisha),
  VENETA(R.string.veneta),
  VIZ_MEDIA(R.string.viz_media)
}
