package io.github.alessandrojean.toshokan.service.cover

import androidx.annotation.StringRes
import io.github.alessandrojean.toshokan.R

enum class CoverProviderWebsite(@StringRes val title: Int, val url: String) {
  AMAZON(R.string.amazon, "https://amazon.com"),
  LOJA_PANINI(R.string.loja_panini, "https://loja.panini.com.br"),
  JBC(R.string.jbc, "https://editorajbc.com.br"),
  NEWPOP(R.string.newpop, "https://www.newpop.com.br"),
  PIPOCA_E_NANQUIM(R.string.pipoca_e_nanquim, "https://pipocaenanquim.com.br"),
  SHUEISHA(R.string.shueisha, "https://www.shueisha.co.jp"),
  VENETA(R.string.veneta, "https://veneta.com.br"),
  VIZ_MEDIA(R.string.viz_media, "https://www.viz.com/read")
}
