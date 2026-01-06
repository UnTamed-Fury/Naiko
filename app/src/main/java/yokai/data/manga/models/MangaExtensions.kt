package yokai.data.manga.models

import eu.kanade.tachiyomi.source.model.SManga
import yokai.domain.manga.models.Manga

fun Manga.toSManga() = SManga.create().also {
    it.url = url
    it.title = title
    it.artist = artist
    it.author = author
    it.description = description
    it.genre = genres.orEmpty().joinToString()
    it.status = status
    it.thumbnail_url = thumbnailUrl
    it.initialized = initialized
}

fun Manga.copyFrom(other: SManga): Manga {
    val author = other.author ?: author
    val artist = other.artist ?: artist
    val description = other.description ?: description
    val genres = if (other.genre != null) other.getGenres() else genres
    val thumbnailUrl = other.thumbnail_url ?: thumbnailUrl
    return this.copy(
        author = author,
        artist = artist,
        description = description,
        genres = genres,
        thumbnailUrl = thumbnailUrl,
    )
}
