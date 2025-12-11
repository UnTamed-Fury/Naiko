package eu.kanade.tachiyomi.data.database.models

import eu.kanade.tachiyomi.data.download.anime.AnimeDownloadManager
import eu.kanade.tachiyomi.data.download.anime.AnimeDownloadProvider
// import eu.kanade.tachiyomi.data.library.CustomAnimeManager
import eu.kanade.tachiyomi.domain.anime.models.Anime
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.AnimeUpdateStrategy
import uy.kohesive.injekt.injectLazy

open class AnimeImpl(
    override var id: Long? = null,
    override var source: Long = -1,
    override var url: String = "",
) : Anime {

    // private val customAnimeManager: CustomAnimeManager by injectLazy()

    override var title: String
        get() = ogTitle // if (favorite) { val customTitle = customAnimeManager.getAnime(this)?.title; if (customTitle.isNullOrBlank()) ogTitle else customTitle } else { ogTitle }
        set(value) {
            ogTitle = value
        }

    override var author: String?
        get() = ogAuthor // if (favorite) customAnimeManager.getAnime(this)?.author ?: ogAuthor else ogAuthor
        set(value) { ogAuthor = value }

    override var artist: String?
        get() = ogArtist // if (favorite) customAnimeManager.getAnime(this)?.artist ?: ogArtist else ogArtist
        set(value) { ogArtist = value }

    override var description: String?
        get() = ogDesc // if (favorite) customAnimeManager.getAnime(this)?.description ?: ogDesc else ogDesc
        set(value) { ogDesc = value }

    override var genre: String?
        get() = ogGenre // if (favorite) customAnimeManager.getAnime(this)?.genre ?: ogGenre else ogGenre
        set(value) { ogGenre = value }

    override var status: Int
        get() = ogStatus // if (favorite) { customAnimeManager.getAnime(this)?.status.takeIf { it != -1 } ?: ogStatus } else { ogStatus }
        set(value) { ogStatus = value }

    override var thumbnail_url: String? = null

    override var favorite: Boolean = false

    override var last_update: Long = 0

    override var initialized: Boolean = false

    override var viewer_flags: Int = -1

    override var episode_flags: Int = 0

    override var hide_title: Boolean = false

    override var date_added: Long = 0

    override var update_strategy: AnimeUpdateStrategy = AnimeUpdateStrategy.ALWAYS_UPDATE

    // TODO: It's probably fine to set this to non-null string in the future
    override var filtered_scanlators: String? = ""

    override lateinit var ogTitle: String
    override var ogAuthor: String? = null
    override var ogArtist: String? = null
    override var ogDesc: String? = null
    override var ogGenre: String? = null
    override var ogStatus: Int = 0

    override var cover_last_modified: Long = 0L

    override fun copyFrom(other: SAnime) {
        val remoteTitle = try {
            if (other is Anime) other.ogTitle else other.title
        } catch (_: UninitializedPropertyAccessException) {
            ""
        }

        if (remoteTitle.isNotBlank() && remoteTitle != this.ogTitle) {
            val oldTitle = this.ogTitle
            this.ogTitle = remoteTitle

            val db: AnimeDownloadManager by injectLazy()
            val provider = AnimeDownloadProvider(db.context)
            provider.renameAnimeFolder(oldTitle, this.ogTitle, source)
        }
        super.copyFrom(other)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val anime = other as Anime

        return url == anime.url && source == anime.source
    }

    override fun hashCode(): Int {
        return if (url.isNotBlank()) {
            url.hashCode()
        } else {
            (id ?: 0L).hashCode()
        }
    }
}