package eu.kanade.tachiyomi.data.download.anime

import android.content.Context
import com.hippo.unifile.UniFile
import eu.kanade.tachiyomi.data.database.models.Episode
import eu.kanade.tachiyomi.domain.anime.models.Anime
import eu.kanade.tachiyomi.animesource.AnimeSource
import eu.kanade.tachiyomi.util.storage.DiskUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import uy.kohesive.injekt.injectLazy
import yokai.domain.download.DownloadPreferences
import yokai.domain.storage.StorageManager
import yokai.i18n.MR
import yokai.util.lang.getString

class AnimeDownloadProvider(private val context: Context) {

    private val downloadPreferences: DownloadPreferences by injectLazy()
    private val storageManager: StorageManager by injectLazy()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var downloadsDir = storageManager.getDownloadsDirectory()

    init {
        storageManager.changes.onEach {
            downloadsDir = storageManager.getDownloadsDirectory()
        }.launchIn(scope)
    }

    internal fun getAnimeDir(anime: Anime, source: AnimeSource): UniFile {
        try {
            return downloadsDir?.createDirectory(getSourceDirName(source))!!
                .createDirectory(getAnimeDirName(anime))!!
        } catch (e: NullPointerException) {
            throw Exception(context.getString(MR.strings.invalid_download_location))
        }
    }

    fun findSourceDir(source: AnimeSource): UniFile? {
        return downloadsDir?.findFile(getSourceDirName(source))
    }

    fun findAnimeDir(anime: Anime, source: AnimeSource): UniFile? {
        val sourceDir = findSourceDir(source)
        return sourceDir?.findFile(getAnimeDirName(anime))
    }

    fun findEpisodeDir(episode: Episode, anime: Anime, source: AnimeSource): UniFile? {
        val animeDir = findAnimeDir(anime, source)
        return getValidEpisodeDirNames(episode).asSequence()
            .mapNotNull { animeDir?.findFile(it) }
            .firstOrNull()
    }

    fun findEpisodeDirs(episodes: List<Episode>, anime: Anime, source: AnimeSource): List<UniFile> {
        val animeDir = findAnimeDir(anime, source) ?: return emptyList()
        return episodes.mapNotNull { episode ->
            getValidEpisodeDirNames(episode).asSequence()
                .mapNotNull { animeDir.findFile(it) }
                .firstOrNull()
        }
    }

    fun getSourceDirName(source: AnimeSource): String {
        return source.toString()
    }

    fun getAnimeDirName(anime: Anime): String {
        return DiskUtil.buildValidFilename(anime.originalTitle)
    }

    fun getEpisodeDirName(episode: Episode, includeBlank: Boolean = false, includeId: Boolean = false): String {
         return DiskUtil.buildValidFilename(
            if (!episode.scanlator.isNullOrBlank()) {
                "${episode.scanlator}_${episode.name}"
            } else {
                (if (includeBlank) "_" else "") + episode.name
            } + (if (includeId) "_${episode.id}" else ""),
        )
    }

    fun getValidEpisodeDirNames(episode: Episode): List<String> {
        return buildList {
            add(getEpisodeDirName(episode))
            add(getEpisodeDirName(episode, includeBlank = true))
            add(getEpisodeDirName(episode, includeBlank = false, includeId = true))
            add(getEpisodeDirName(episode, includeBlank = true, includeId = true))
            add(DiskUtil.buildValidFilename(episode.name))
        }.distinct()
    }
}