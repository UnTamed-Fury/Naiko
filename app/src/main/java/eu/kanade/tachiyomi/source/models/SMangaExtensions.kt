package eu.kanade.tachiyomi.source.models

import eu.kanade.tachiyomi.domain.manga.models.Manga
import eu.kanade.tachiyomi.source.model.SManga

val SManga.originalTitle: String
    get() = if (this is Manga) this.originalTitle else title
