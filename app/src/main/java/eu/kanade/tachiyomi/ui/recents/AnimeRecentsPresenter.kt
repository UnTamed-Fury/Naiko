package eu.kanade.tachiyomi.ui.recents

import eu.kanade.tachiyomi.data.database.models.Episode
import eu.kanade.tachiyomi.data.database.models.AnimeEpisodeHistory
import eu.kanade.tachiyomi.data.download.anime.AnimeDownloadManager
import eu.kanade.tachiyomi.data.download.model.AnimeDownload
import eu.kanade.tachiyomi.data.download.model.AnimeDownloadQueue
import eu.kanade.tachiyomi.data.library.LibraryUpdateJob
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.domain.anime.models.Anime
import eu.kanade.tachiyomi.ui.base.presenter.BaseCoroutinePresenter
import eu.kanade.tachiyomi.util.system.launchIO
import eu.kanade.tachiyomi.util.system.launchUI
import eu.kanade.tachiyomi.util.system.withUIContext
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import uy.kohesive.injekt.injectLazy
import yokai.domain.recents.RecentsPreferences
import yokai.domain.ui.UiPreferences

class AnimeRecentsPresenter(
    val uiPreferences: UiPreferences = Injekt.get(),
    val recentsPreferences: RecentsPreferences = Injekt.get(),
    val preferences: PreferencesHelper = Injekt.get(),
    val downloadManager: AnimeDownloadManager = Injekt.get(),
) : BaseCoroutinePresenter<RecentsController>(), // Should be AnimeRecentsController
    AnimeDownloadQueue.Listener {

    private var recentsJob: Job? = null
    var recentItems = listOf<RecentAnimeItem>()
        private set
    var query = ""
        set(value) {
            field = value
        }
    
    var viewType: RecentsViewType = RecentsViewType.valueOf(uiPreferences.recentsViewType().get())
        private set
    
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var isLoading = false
        private set

    override val progressJobs = mutableMapOf<AnimeDownload, Job>()
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
                withUIContext {
                    // Update view logic here
                    // view?.updateDownloadStatus(!downloadManager.isPaused())
                }
            }
        }
        
        getRecents()
    }

    fun getRecents() {
        // Fetch recents logic
    }
    
    fun toggleGroupRecents(pref: RecentsViewType) {
        viewType = pref
        getRecents()
    }

    override fun onProgressUpdate(download: AnimeDownload) {
        // view?.updateEpisodeDownload(download)
    }

    override fun onQueueUpdate(download: AnimeDownload) {
        // view?.updateEpisodeDownload(download)
    }
}
