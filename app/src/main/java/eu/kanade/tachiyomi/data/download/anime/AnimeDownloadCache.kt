package eu.kanade.tachiyomi.data.download.anime

import android.content.Context
import eu.kanade.tachiyomi.data.database.models.Episode
import eu.kanade.tachiyomi.domain.anime.models.Anime
import eu.kanade.tachiyomi.animesource.AnimeSourceManager
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class AnimeDownloadCache(
    private val context: Context,
    private val provider: AnimeDownloadProvider = Injekt.get(),
    private val sourceManager: AnimeSourceManager = Injekt.get(),
) {

    fun isEpisodeDownloaded(episode: Episode, anime: Anime, skipCache: Boolean): Boolean {
        // Simplified implementation: always check provider (filesystem)
        val source = sourceManager.get(anime.source) ?: return false
        return provider.findEpisodeDir(episode, anime, source) != null
    }

    fun getDownloadCount(anime: Anime, forceCheckFolder: Boolean = false): Int {
         val source = sourceManager.get(anime.source) ?: return 0
         val animeDir = provider.findAnimeDir(anime, source)
         // Count files/dirs that look like episodes
         return animeDir?.listFiles()?.count { it.isDirectory } ?: 0
    }

    fun removeEpisodes(episodes: List<Episode>, anime: Anime) {
        // No-op for direct filesystem check
    }
    
    fun forceRenewCache() {
        // No-op
    }
}