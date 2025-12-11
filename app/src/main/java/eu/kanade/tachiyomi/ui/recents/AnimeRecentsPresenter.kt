package eu.kanade.tachiyomi.ui.recents

import eu.kanade.tachiyomi.data.database.models.Episode
import eu.kanade.tachiyomi.data.database.models.EpisodeHistory
import eu.kanade.tachiyomi.data.database.models.AnimeHistory
import eu.kanade.tachiyomi.data.database.models.AnimeHistoryImpl
import eu.kanade.tachiyomi.data.database.models.AnimeEpisodeHistory
import eu.kanade.tachiyomi.data.download.anime.AnimeDownloadManager
import eu.kanade.tachiyomi.data.download.model.Download
import eu.kanade.tachiyomi.data.download.model.DownloadQueue
import eu.kanade.tachiyomi.data.library.LibraryUpdateJob
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.domain.anime.models.Anime
import eu.kanade.tachiyomi.source.anime.AnimeSourceManager
import eu.kanade.tachiyomi.ui.base.presenter.BaseCoroutinePresenter
import eu.kanade.tachiyomi.util.episode.EpisodeFilter
import eu.kanade.tachiyomi.util.episode.EpisodeSort
import eu.kanade.tachiyomi.util.system.launchIO
import eu.kanade.tachiyomi.util.system.launchNonCancellableIO
import eu.kanade.tachiyomi.util.system.launchUI
import eu.kanade.tachiyomi.util.system.toast
import eu.kanade.tachiyomi.util.system.withUIContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TreeMap
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import uy.kohesive.injekt.injectLazy
import yokai.data.DatabaseHandler
import yokai.domain.episode.interactor.GetEpisode
import yokai.domain.episode.interactor.UpdateEpisode
import yokai.domain.history.interactor.GetHistory
import yokai.domain.history.interactor.UpsertHistory
import yokai.domain.recents.RecentsPreferences
// import yokai.domain.recents.interactor.GetAnimeRecents // Need to implement this
import yokai.domain.ui.UiPreferences
import yokai.i18n.MR

class AnimeRecentsPresenter(
    val uiPreferences: UiPreferences = Injekt.get(),
    val recentsPreferences: RecentsPreferences = Injekt.get(),
    val preferences: PreferencesHelper = Injekt.get(),
    val downloadManager: AnimeDownloadManager = Injekt.get(),
    private val episodeFilter: EpisodeFilter = Injekt.get(),
) : BaseCoroutinePresenter<RecentsController>(),
    DownloadQueue.Listener {
    private val handler: DatabaseHandler by injectLazy()

    private val getEpisode: GetEpisode by injectLazy()
    // private val getRecents: GetAnimeRecents by injectLazy()
    private val updateEpisode: UpdateEpisode by injectLazy()
    private val getHistory: GetHistory by injectLazy()
    private val upsertHistory: UpsertHistory by injectLazy()

    private var recentsJob: Job? = null
    var recentItems = listOf<RecentAnimeItem>()
        private set
    var query = ""
        set(value) {
            field = value
            resetOffsets()
        }
    private val newAdditionsHeader = RecentAnimeHeaderItem(RecentAnimeHeaderItem.NEWLY_ADDED)
    private val newEpisodesHeader = RecentAnimeHeaderItem(RecentAnimeHeaderItem.NEW_CHAPTERS) // Reusing CHAPTERS constant for Episodes
    private val continueReadingHeader = RecentAnimeHeaderItem(RecentAnimeHeaderItem.CONTINUE_READING)

    var finished = false
    private var shouldMoveToTop = false
    var viewType: RecentsViewType = RecentsViewType.valueOf(uiPreferences.recentsViewType().get())
        private set
    var groupHistory: GroupType = preferences.groupChaptersHistory().get()
        private set
    val expandedSectionsMap = mutableMapOf<String, Boolean>()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private fun resetOffsets() {
        finished = false
        shouldMoveToTop = true
        pageOffset = 0
        expandedSectionsMap.clear()
    }

    private var pageOffset = 0
    var isLoading = false
        private set

    private val isOnFirstPage: Boolean
        get() = pageOffset == 0

    override val progressJobs = mutableMapOf<Download, Job>()
    override val queueListenerScope get() = presenterScope

    override fun onCreate() {
        super.onCreate()
        presenterScope.launchUI {
            downloadManager.statusFlow().collect(::onStatusChange)
        }
        presenterScope.launchUI {
            downloadManager.progressFlow().collect(::onProgressUpdate)
        }
        presenterScope.launchIO {
            downloadManager.queueState.collectLatest {
                if (recentItems.isNotEmpty()) setDownloadedEpisodes(recentItems, it)
                withUIContext {
                    if (recentItems.isNotEmpty()) view?.showLists(recentItems, true)
                    view?.updateDownloadStatus(!downloadManager.isPaused())
                }
            }
        }
        downloadManager.isDownloaderRunning.onEach(::downloadStatusChanged).launchIn(presenterScope)
        LibraryUpdateJob.updateFlow.onEach(::onUpdateAnime).launchIn(presenterScope)
        if (lastRecents != null) {
            if (recentItems.isEmpty()) {
                recentItems = lastRecents ?: emptyList()
            }
            lastRecents = null
        }
        getRecents()
        listOf(
            preferences.groupChaptersHistory(),
            recentsPreferences.showReadInAllRecents(),
            preferences.sortFetchedTime(),
        ).forEach {
            it.changes()
                .drop(1)
                .onEach {
                    resetOffsets()
                    getRecents()
                }
                .launchIn(presenterScope)
        }
    }

    fun getRecents(updatePageCount: Boolean = false) {
        val oldQuery = query
        recentsJob?.cancel()
        recentsJob = presenterScope.launch {
            runRecents(oldQuery, updatePageCount)
        }
    }

    private suspend fun runRecents(
        oldQuery: String = "",
        updatePageCount: Boolean = false,
        retryCount: Int = 0,
        itemCount: Int = 0,
        limit: Int = -1,
        customViewType: RecentsViewType? = null,
        includeReadAnyway: Boolean = false,
    ) {
        // Implementation omitted for brevity, logic needs to be adapted for anime similar to manga
    }

    // Helper methods (setupExtraEpisodes, etc.) adapted for anime...

    override fun onDestroy() {
        super.onDestroy()
        lastRecents = recentItems
    }

    fun toggleGroupRecents(pref: RecentsViewType, updatePref: Boolean = true) {
        if (updatePref) {
            uiPreferences.recentsViewType().set(pref.mainValue)
        }
        viewType = pref
        resetOffsets()
        getRecents()
    }

    private fun setDownloadedEpisodes(episodes: List<RecentAnimeItem>, queue: List<Download> = downloadManager.queueState.value) {
        // Implementation adapted for anime downloads
    }

    private fun downloadStatusChanged(downloading: Boolean) {
        presenterScope.launchUI {
            view?.updateDownloadStatus(downloading)
        }
    }

    private fun onUpdateAnime(animeId: Long?) {
        when (animeId) {
            null -> {
                presenterScope.launchUI { view?.setRefreshing(false) }
            }
            LibraryUpdateJob.STARTING_UPDATE_SOURCE -> {
                presenterScope.launchUI { view?.setRefreshing(true) }
            }
            else -> {
                getRecents()
            }
        }
    }

    fun deleteEpisode(episode: Episode, anime: Anime, update: Boolean = true) {
        val source = Injekt.get<AnimeSourceManager>().getOrStub(anime.source)
        launchIO {
            downloadManager.deleteEpisodes(listOf(episode), anime, source)
        }
        if (update) {
            // Update logic...
        }
    }

    // ... other methods adapted for anime

    override fun onProgressUpdate(download: Download) {
        // don't do anything
    }

    override fun onQueueUpdate(download: Download) {
        view?.updateChapterDownload(download)
    }

    override fun onPageProgressUpdate(download: Download) {
        view?.updateChapterDownload(download)
    }

    enum class GroupType {
        BySeries,
        ByWeek,
        ByDay,
        Never,
        ;

        val isByTime get() = this == ByWeek || this == ByDay
    }

    companion object {
        private var lastRecents: List<RecentAnimeItem>? = null

        fun onLowMemory() {
            lastRecents = null
        }

        const val ENDLESS_LIMIT = 50
        var SHORT_LIMIT = 25
            private set

        suspend fun getNextEpisode(
            anime: Anime,
            getEpisode: GetEpisode = Injekt.get(),
            episodeFilter: EpisodeFilter = Injekt.get(),
            preferences: PreferencesHelper = Injekt.get(),
        ): Episode? {
            val animeId = anime.id ?: return null
            val episodes = getEpisode.awaitUnseen(animeId)
            return EpisodeSort(anime, episodeFilter, preferences).getNextUnseenEpisode(episodes, false)
        }

        suspend fun getFirstUpdatedEpisode(
            anime: Anime,
            episode: Episode,
            getEpisode: GetEpisode = Injekt.get(),
            episodeFilter: EpisodeFilter = Injekt.get(),
            preferences: PreferencesHelper = Injekt.get(),
        ): Episode? {
            val animeId = anime.id ?: return null
            val episodes = getEpisode.awaitUnseen(animeId)
            return episodes.sortedWith(EpisodeSort(anime, episodeFilter, preferences).sortComparator(true)).find {
                abs(it.date_fetch - episode.date_fetch) <= TimeUnit.HOURS.toMillis(12)
            }
        }

        suspend fun getRecentAnime(includeSeen: Boolean = false, customAmount: Int = 0): List<Pair<Anime, Long>> {
            val presenter = AnimeRecentsPresenter()
            presenter.viewType = RecentsViewType.UngroupedAll
            SHORT_LIMIT = when {
                customAmount > 0 -> (customAmount * 1.5).roundToInt()
                includeSeen -> 50
                else -> 25
            }
            presenter.runRecents(limit = customAmount, includeReadAnyway = includeSeen)
            SHORT_LIMIT = 25
            return presenter.recentItems
                .filter { it.aeh.anime.id != null }
                .map { it.aeh.anime to it.aeh.history.last_seen }
        }

        const val UPDATES_CHAPTER_LIMIT = 4
        const val UPDATES_READING_LIMIT_UPPER = 9
        const val UPDATES_READING_LIMIT_LOWER = 5
    }
}