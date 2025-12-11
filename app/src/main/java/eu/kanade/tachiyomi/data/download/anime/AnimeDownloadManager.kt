package eu.kanade.tachiyomi.data.download.anime

import android.content.Context
import com.hippo.unifile.UniFile
import eu.kanade.tachiyomi.data.database.models.Episode
import eu.kanade.tachiyomi.data.download.model.AnimeDownload
import eu.kanade.tachiyomi.data.download.model.Download
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.domain.anime.models.Anime
import eu.kanade.tachiyomi.animesource.AnimeSource
import eu.kanade.tachiyomi.animesource.AnimeSourceManager
import eu.kanade.tachiyomi.util.system.launchIO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import uy.kohesive.injekt.injectLazy
import yokai.domain.download.DownloadPreferences

class AnimeDownloadManager(
    val context: Context,
    private val sourceManager: AnimeSourceManager = Injekt.get(),
    private val provider: AnimeDownloadProvider = Injekt.get(),
    private val cache: AnimeDownloadCache = Injekt.get(),
) {

    private val preferences by injectLazy<PreferencesHelper>()
    private val downloadPreferences by injectLazy<DownloadPreferences>()

    private val downloader = AnimeDownloader(context)

    val isRunning: Boolean get() = downloader.isRunning

    val queueState get() = downloader.queueState

    // val isDownloaderRunning get() = AnimeDownloadJob.isRunningFlow(context) // Assuming AnimeDownloadJob

    fun startDownloads(): Boolean {
        return downloader.start()
    }

    fun stopDownloads(reason: String? = null) = downloader.stop(reason)

    fun pauseDownloads() {
        downloader.pause()
        downloader.stop()
    }

    fun clearQueue() {
        downloader.clearQueue()
        downloader.stop()
    }

    fun startDownloadNow(episode: Episode) {
        val download = queueState.value.find { it.episode.id == episode.id } ?: return
        val queue = queueState.value.toMutableList()
        queue.remove(download)
        queue.add(0, download)
        reorderQueue(queue)
        if (isPaused()) {
            downloader.start()
        }
    }

    fun reorderQueue(downloads: List<AnimeDownload>) {
        downloader.updateQueue(downloads)
    }

    fun isPaused() = !downloader.isRunning

    fun hasQueue() = queueState.value.isNotEmpty()

    fun downloadEpisodes(anime: Anime, episodes: List<Episode>, autoStart: Boolean = true) {
        downloader.queueEpisodes(anime, episodes, autoStart)
    }

    fun isEpisodeDownloaded(episode: Episode, anime: Anime, skipCache: Boolean = false): Boolean {
        return cache.isEpisodeDownloaded(episode, anime, skipCache)
    }

    fun getEpisodeDownloadOrNull(episode: Episode): AnimeDownload? {
        return queueState.value
            .firstOrNull { it.episode.id == episode.id && it.episode.anime_id == episode.anime_id }
    }

    fun getDownloadCount(anime: Anime): Int {
        return cache.getDownloadCount(anime)
    }

    fun deleteEpisodes(episodes: List<Episode>, anime: Anime, source: AnimeSource) {
        launchIO {
            val filteredEpisodes = episodes // getEpisodesToDelete(episodes, anime)
            if (filteredEpisodes.isEmpty()) {
                return@launchIO
            }

            removeFromDownloadQueue(filteredEpisodes)

            val episodeDirs = provider.findEpisodeDirs(filteredEpisodes, anime, source)
            episodeDirs.forEach { it.delete() }
            cache.removeEpisodes(filteredEpisodes, anime)

            if (cache.getDownloadCount(anime, true) == 0) {
                episodeDirs.firstOrNull()?.parentFile?.delete()
            }
        }
    }

    private fun removeFromDownloadQueue(episodes: List<Episode>) {
        val wasRunning = downloader.isRunning
        if (wasRunning) {
            downloader.pause()
        }

        downloader.removeFromQueue(episodes)

        if (wasRunning) {
            if (queueState.value.isEmpty()) {
                downloader.stop()
            } else if (queueState.value.isNotEmpty()) {
                downloader.start()
            }
        }
    }

    fun statusFlow(): Flow<AnimeDownload> = queueState
        .flatMapLatest { downloads ->
            downloads
                .map { download ->
                    download.statusFlow.drop(1).map { download }
                }
                .merge()
        }
        .onStart {
            emitAll(
                queueState.value.filter { download -> download.status == Download.State.DOWNLOADING }.asFlow(),
            )
        }

    fun progressFlow(): Flow<AnimeDownload> = queueState
        .flatMapLatest { downloads ->
            downloads
                .map { download ->
                    download.progressFlow.drop(1).map { download }
                }
                .merge()
        }
        .onStart {
            emitAll(
                queueState.value.filter { download -> download.status == Download.State.DOWNLOADING }
                    .asFlow(),
            )
        }
}