package io.github.alessandrojean.toshokan.data.coil

/**
 * Custom cover keyer implementation to allow custom covers.
 *
 * Based on Tachiyomi's MangaCoverKeyer implementation.
 * https://github.com/tachiyomiorg/tachiyomi
 *
 * Copyright 2015 Javier Tomás
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */

import coil.key.Keyer
import coil.request.Options
import io.github.alessandrojean.toshokan.database.data.Book

class BookCoverKeyer : Keyer<Book> {
  override fun key(data: Book, options: Options): String? {
    return data.cover_url?.takeIf { it.isNotBlank() }
  }
}
