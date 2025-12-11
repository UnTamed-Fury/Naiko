package eu.kanade.tachiyomi.data.download.anime

import android.content.Context
import eu.kanade.tachiyomi.data.database.models.Episode
import eu.kanade.tachiyomi.data.download.model.AnimeDownload
import eu.kanade.tachiyomi.data.download.model.Download
import eu.kanade.tachiyomi.domain.anime.models.Anime
import eu.kanade.tachiyomi.animesource.AnimeSourceManager
import eu.kanade.tachiyomi.animesource.online.AnimeHttpSource
import eu.kanade.tachiyomi.util.system.launchIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class AnimeDownloader(
    private val context: Context,
    private val provider: AnimeDownloadProvider = Injekt.get(),
    private val cache: AnimeDownloadCache = Injekt.get(),
    private val sourceManager: AnimeSourceManager = Injekt.get(),
) {
    private val _queueState = MutableStateFlow<List<AnimeDownload>>(emptyList())
    val queueState = _queueState.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var downloaderJob: Job? = null

    val isRunning: Boolean
        get() = downloaderJob?.isActive ?: false

    @Volatile
    var isPaused: Boolean = false

    fun start(): Boolean {
        if (isRunning || queueState.value.isEmpty()) {
            return false
        }

        val pending = queueState.value.filter { it.status != Download.State.DOWNLOADED }
        pending.forEach { if (it.status != Download.State.QUEUE) it.status = Download.State.QUEUE }

        isPaused = false
        launchDownloaderJob()
        return pending.isNotEmpty()
    }

    fun stop(reason: String? = null) {
        cancelDownloaderJob()
        queueState.value
            .filter { it.status == Download.State.DOWNLOADING }
            .forEach { it.status = Download.State.ERROR }
        isPaused = false
    }

    fun pause() {
        cancelDownloaderJob()
        queueState.value
            .filter { it.status == Download.State.DOWNLOADING }
            .forEach { it.status = Download.State.QUEUE }
        isPaused = true
    }

    fun clearQueue() {
        cancelDownloaderJob()
        _queueState.update { emptyList() }
    }

    private fun launchDownloaderJob() {
        if (isRunning) return

        downloaderJob = scope.launch {
            val activeDownloadsFlow = queueState.transformLatest { queue ->
                while (true) {
                    val activeDownloads = queue.asSequence()
                        .filter {
                            val statusValue = it.status.value
                            Download.State.NOT_DOWNLOADED.value <= statusValue && statusValue <= Download.State.DOWNLOADING.value
                        }
                        .groupBy { it.source }
                        .toList()
                        .take(5)
                        .map { (_, downloads) -> downloads.first() }
                    emit(activeDownloads)
                    if (activeDownloads.isEmpty()) break
                    kotlinx.coroutines.delay(1000) // Simple polling for now
                }
            }.distinctUntilChanged()

            supervisorScope {
                val downloadJobs = mutableMapOf<AnimeDownload, Job>()
                activeDownloadsFlow.collectLatest { activeDownloads ->
                    val downloadJobsToStop = downloadJobs.filter { it.key !in activeDownloads }
                    downloadJobsToStop.forEach { (download, job) ->
                        job.cancel()
                        downloadJobs.remove(download)
                    }

                    val downloadsToStart = activeDownloads.filter { it !in downloadJobs }
                    downloadsToStart.forEach { download ->
                        downloadJobs[download] = launchDownloadJob(download)
                    }
                }
            }
        }
    }

    private fun CoroutineScope.launchDownloadJob(download: AnimeDownload) = launchIO {
        try {
            downloadEpisode(download)
            if (download.status == Download.State.DOWNLOADED) {
                removeFromQueue(download)
            }
        } catch (e: Throwable) {
            download.status = Download.State.ERROR
        }
    }

    private fun cancelDownloaderJob() {
        downloaderJob?.cancel()
        downloaderJob = null
    }

    fun queueEpisodes(anime: Anime, episodes: List<Episode>, autoStart: Boolean) = launchIO {
        if (episodes.isEmpty()) return@launchIO
        val source = sourceManager.get(anime.source) as? AnimeHttpSource ?: return@launchIO
        
        val episodesToQueue = episodes
            .filter { episode -> queueState.value.none { it.episode.id == episode.id } }
            .map { AnimeDownload(source, anime, it) }

        if (episodesToQueue.isNotEmpty()) {
            addAllToQueue(episodesToQueue)
            if (autoStart) start()
        }
    }

    private suspend fun downloadEpisode(download: AnimeDownload) {
        // Placeholder implementation
        download.status = Download.State.DOWNLOADING
        // Simulate download
        kotlinx.coroutines.delay(1000)
        download.status = Download.State.DOWNLOADED
        // Update cache/provider
        // cache.addEpisode(...)
    }

    private fun addAllToQueue(downloads: List<AnimeDownload>) {
        _queueState.update {
            downloads.forEach { download -> download.status = Download.State.QUEUE }
            it + downloads
        }
    }

    fun removeFromQueue(download: AnimeDownload) {
        _queueState.update {
            if (download.status == Download.State.DOWNLOADING || download.status == Download.State.QUEUE) {
                download.status = Download.State.NOT_DOWNLOADED
            }
            it - download
        }
    }

    fun removeFromQueue(episodes: List<Episode>) {
        _queueState.update { queue ->
            val downloads = queue.filter { it.episode.id in episodes.map { ep -> ep.id } }
            downloads.forEach { it.status = Download.State.NOT_DOWNLOADED }
            queue - downloads
        }
    }

    fun updateQueue(downloads: List<AnimeDownload>) {
        val wasRunning = isRunning
        if (downloads.isEmpty()) {
            clearQueue()
            return
        }
        pause()
        _queueState.update { emptyList() } // Clear internal
        addAllToQueue(downloads)
        if (wasRunning) start()
    }
}