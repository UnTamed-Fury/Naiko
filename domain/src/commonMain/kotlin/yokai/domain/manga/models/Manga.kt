package yokai.domain.manga.models

import eu.kanade.tachiyomi.source.model.UpdateStrategy
import java.io.Serializable

data class Manga(
    val id: Long?,
    val url: String,
    val title: String,
    val artist: String?,
    val author: String?,
    val description: String?,
    val genres: List<String>?,
    val status: Int,
    val thumbnailUrl: String?,
    val updateStrategy: UpdateStrategy,
    val initialized: Boolean,
    var source: Long,
    var favorite: Boolean,
    var lastUpdate: Long,
    var dateAdded: Long,
    var viewerFlags: Int,
    var chapterFlags: Int,
    var hideTitle: Boolean,
    var filteredScanlators: String?,
    var coverLastModified: Long,
): Serializable
