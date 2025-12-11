package yokai.domain.manga.models

import eu.kanade.tachiyomi.domain.manga.models.Manga as TachiManga

data class MangaCover(
    val mangaId: Long?,
    val sourceId: Long,
    val url: String,
    val lastModified: Long,
    val inLibrary: Boolean,
)

fun TachiManga.cover() = MangaCover(
    mangaId = id,
    sourceId = source,
    url = thumbnail_url ?: "",
    lastModified = cover_last_modified,
    inLibrary = favorite,
)

fun Manga.cover() = MangaCover(
    mangaId = id,
    sourceId = source,
    url = thumbnailUrl ?: "",
    lastModified = coverLastModified,
    inLibrary = favorite,
)
