package eu.kanade.tachiyomi.ui.library

import eu.kanade.tachiyomi.core.preference.minusAssign
import eu.kanade.tachiyomi.core.preference.plusAssign
import eu.kanade.tachiyomi.data.cache.CoverCache
import eu.kanade.tachiyomi.data.database.models.Category
import eu.kanade.tachiyomi.data.database.models.Category.Companion.langSplitter
import eu.kanade.tachiyomi.data.database.models.Category.Companion.sourceSplitter
import eu.kanade.tachiyomi.data.database.models.Episode
import eu.kanade.tachiyomi.data.database.models.Episode.Companion.copy
import eu.kanade.tachiyomi.data.database.models.LibraryAnime
import eu.kanade.tachiyomi.data.database.models.Track
import eu.kanade.tachiyomi.data.database.models.removeCover
import eu.kanade.tachiyomi.data.database.models.seriesType
import eu.kanade.tachiyomi.data.download.anime.AnimeDownloadCache
import eu.kanade.tachiyomi.data.download.anime.AnimeDownloadManager
import eu.kanade.tachiyomi.data.preference.DelayedLibrarySuggestionsJob
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.data.track.TrackManager
import eu.kanade.tachiyomi.domain.anime.models.Anime
import eu.kanade.tachiyomi.animesource.LocalAnimeSource
import eu.kanade.tachiyomi.source.anime.AnimeSourceManager
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.online.AnimeHttpSource
import eu.kanade.tachiyomi.ui.base.presenter.BaseCoroutinePresenter
import eu.kanade.tachiyomi.ui.library.LibraryGroup.BY_AUTHOR
import eu.kanade.tachiyomi.ui.library.LibraryGroup.BY_DEFAULT
import eu.kanade.tachiyomi.ui.library.LibraryGroup.BY_LANGUAGE
import eu.kanade.tachiyomi.ui.library.LibraryGroup.BY_SOURCE
import eu.kanade.tachiyomi.ui.library.LibraryGroup.BY_TAG
import eu.kanade.tachiyomi.ui.library.LibraryGroup.BY_TRACK_STATUS
import eu.kanade.tachiyomi.ui.library.LibraryGroup.UNGROUPED
import eu.kanade.tachiyomi.ui.library.filter.FilterBottomSheet
import eu.kanade.tachiyomi.ui.library.filter.FilterBottomSheet.Companion.STATE_EXCLUDE
import eu.kanade.tachiyomi.ui.library.filter.FilterBottomSheet.Companion.STATE_IGNORE
import eu.kanade.tachiyomi.ui.library.filter.FilterBottomSheet.Companion.STATE_INCLUDE
import eu.kanade.tachiyomi.ui.recents.RecentsPresenter
import eu.kanade.tachiyomi.util.episode.EpisodeFilter
import eu.kanade.tachiyomi.util.episode.EpisodeSort
import eu.kanade.tachiyomi.util.isLocal
import eu.kanade.tachiyomi.util.lang.capitalizeWords
import eu.kanade.tachiyomi.util.lang.chopByWords
import eu.kanade.tachiyomi.util.lang.removeArticles
import eu.kanade.tachiyomi.util.anime.AnimeCoverMetadata
import eu.kanade.tachiyomi.util.mapStatus
import eu.kanade.tachiyomi.util.system.launchIO
import eu.kanade.tachiyomi.util.system.launchNonCancellableIO
import eu.kanade.tachiyomi.util.system.withIOContext
import eu.kanade.tachiyomi.util.system.withUIContext
import java.util.*
import java.util.concurrent.*
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import uy.kohesive.injekt.injectLazy
import naiko.domain.category.interactor.GetCategories
import naiko.domain.category.interactor.SetAnimeCategories
import naiko.domain.category.interactor.UpdateCategories
import naiko.domain.category.models.CategoryUpdate
import naiko.domain.episode.interactor.GetEpisode
import naiko.domain.episode.interactor.UpdateEpisode
import naiko.domain.episode.models.EpisodeUpdate
import naiko.domain.history.interactor.GetHistory
import naiko.domain.library.LibraryPreferences
import naiko.domain.anime.interactor.GetLibraryAnime
import naiko.domain.anime.interactor.GetAnime
import naiko.domain.anime.interactor.UpdateAnime
import naiko.domain.anime.models.AnimeUpdate
import naiko.domain.track.interactor.GetTrack
import naiko.i18n.MR
import naiko.util.isLewd
import naiko.util.lang.getString

typealias LibraryMap = Map<Category, List<LibraryItem>>
typealias LibraryMutableMap = MutableMap<Category, List<LibraryItem>>

/**
 * Presenter of [LibraryController].
 */
class AnimeLibraryPresenter(
    private val preferences: PreferencesHelper = Injekt.get(),
    private val libraryPreferences: LibraryPreferences = Injekt.get(),
    private val coverCache: CoverCache = Injekt.get(),
    val animeSourceManager: AnimeSourceManager = Injekt.get(),
    private val animeDownloadCache: AnimeDownloadCache = Injekt.get(),
    private val animeDownloadManager: AnimeDownloadManager = Injekt.get(),
    private val episodeFilter: EpisodeFilter = Injekt.get(),
    private val trackManager: TrackManager = Injekt.get(),
) : BaseCoroutinePresenter<LibraryController>() {
    private val getCategories: GetCategories by injectLazy()
    private val setAnimeCategories: SetAnimeCategories by injectLazy()
    private val updateCategories: UpdateCategories by injectLazy()
    private val getLibraryAnime: GetLibraryAnime by injectLazy()
    private val getEpisode: GetEpisode by injectLazy()
    private val updateEpisode: UpdateEpisode by injectLazy()
    private val updateAnime: UpdateAnime by injectLazy()
    private val getTrack: GetTrack by injectLazy()
    private val getHistory: GetHistory by injectLazy()

    private val forceUpdateEvent: Channel<Unit> = Channel(Channel.UNLIMITED)

    private val context = preferences.context
    private val viewContext
        get() = view?.view?.context

    private val loggedServices by lazy { trackManager.services.filter { it.isLogged } }

    var groupType = preferences.groupLibraryBy().get()

    val isLoggedIntoTracking
        get() = loggedServices.isNotEmpty()

    /** Current categories of the library. */
    var categories: List<Category> = emptyList()
        private set

    /** All categories of the library, in case they are hidden because of hide categories is on */
    private var allCategories: List<Category> = emptyList()

    private var removeArticles: Boolean = preferences.removeArticles().get()

    /** List of all anime */
    var currentLibrary: LibraryMap = mapOf()
        private set
    val currentLibraryItems: List<LibraryItem>
        get() = currentLibrary.values.flatten()
    /** List of all anime to be displayed */
    private var libraryToDisplay: LibraryMutableMap = mutableMapOf()
    val libraryItemsToDisplay: List<LibraryItem>
        get() = libraryToDisplay.values.flatten()

    var currentCategoryId = -1
        private set
    var currentCategory: Category?
        get() = allCategories.find { it.id == currentCategoryId }
        set(value) { currentCategoryId = value?.id ?: 0 }

    private var hiddenLibraryItems: List<LibraryItem> = emptyList()
    var forceShowAllCategories = false
    val showAllCategories
        get() = forceShowAllCategories || preferences.showAllCategories().get()

    private val libraryIsGrouped
        get() = groupType != UNGROUPED

    private val controllerIsSubClass
        get() = view?.isSubClass == true

    var hasActiveFilters: Boolean = run {
        val filterDownloaded = preferences.filterDownloaded().get()

        val filterUnread = preferences.filterUnread().get()

        val filterCompleted = preferences.filterCompleted().get()

        val filterTracked = preferences.filterTracked().get()

        val filterAnimeType = preferences.filterAnimeType().get()

        val filterContentType = preferences.filterContentType().get()

        !(
            filterDownloaded == 0 &&
            filterUnread == 0 &&
            filterCompleted == 0 &&
            filterTracked == 0 &&
            filterAnimeType == 0 &&
            filterContentType == 0
        )
    }

    fun isCategoryMoreThanOne(): Boolean = allCategories.size > 1

    /** Save the current list to speed up loading later */
    override fun onDestroy() {
        val isSubController = controllerIsSubClass
        super.onDestroy()
        if (!isSubController) {
            lastDisplayedLibrary = libraryToDisplay
            lastCategories = categories
            lastLibrary = currentLibrary
        }
    }

    override fun onCreate() {
        super.onCreate()

        if (!controllerIsSubClass) {
            lastDisplayedLibrary?.let { libraryToDisplay = it }
            lastCategories?.let { categories = it }
            lastLibrary?.let { currentLibrary = it }
            lastCategories = null
            lastDisplayedLibrary = null
            lastLibrary = null
        }

        subscribeLibrary()
        updateLibrary()

        if (!preferences.showLibrarySearchSuggestions().isSet()) {
            DelayedLibrarySuggestionsJob.setupTask(context, true)
        } else if (preferences.showLibrarySearchSuggestions().get() &&
            Date().time >= preferences.lastLibrarySuggestion().get() + TimeUnit.HOURS.toMillis(2)
        ) {
            // Doing this instead of a job in case the app isn't used often
            presenterScope.launch {
                withIOContext { setSearchSuggestion(preferences, getLibraryAnime, animeSourceManager) }
                withUIContext { view?.setTitle() }
            }
        }
    }

    fun getItemCountInCategories(categoryId: Int): Int {
        val category = categories.find { it.id == categoryId }
        val items = libraryToDisplay[category]
        val firstItem = items?.firstOrNull() as? LibraryPlaceholderItem?
        if (firstItem != null) {
            if (firstItem.type !is LibraryPlaceholderItem.Type.Hidden) {
                return 0
            }
            return firstItem.type.hiddenItems.size
        }
        return items?.size ?: 0
    }

    private fun subscribeLibrary() {
        presenterScope.launchIO {
            // Initial setup
            if (categories.isEmpty()) {
                val dbCategories = getCategories.await()
                if ((dbCategories + Category.createDefault(context)).distinctBy { it.order }.size != dbCategories.size + 1) {
                    reorderCategories(dbCategories)
                }
                categories = lastCategories ?: getCategories.await().toMutableList()
            }

            combine(
                getLibraryFlow(),
                animeDownloadCache.changes,
            ) { data, _ -> data }.collectLatest { data ->
                categories = data.categories
                allCategories = data.allCategories

                val library = data.items
                val hiddenItems = data.hiddenItems

                library.forEach { (_, items) ->
                    setDownloadCount(items)
                    setUnreadBadge(items)
                    setSourceLanguage(items)
                }
                setDownloadCount(hiddenItems)
                setUnreadBadge(hiddenItems)
                setSourceLanguage(hiddenItems)

                currentLibrary = library
                hiddenLibraryItems = hiddenItems
                val animeMap = library
                    .applyFilters()
                    .applySort()
                val freshStart = libraryToDisplay.isEmpty()
                sectionLibrary(animeMap, freshStart)
            }
        }
    }

    private suspend fun reorderCategories(categories: List<Category>) {
        val sortedCategories = categories.sortedBy { it.order }
        sortedCategories.forEachIndexed { i, category -> category.order = i }
        updateCategories.await(
            sortedCategories.map { CategoryUpdate(id = it.id!!.toLong(), order = it.order.toLong()) }
        )
    }

    fun switchSection(order: Int) {
        preferences.lastUsedCategory().set(order)
        val category = categories.find { it.order == order } ?: return
        currentCategory = category
        view?.onNextLibraryUpdate(libraryToDisplay[category] ?: blankItem())
    }

    fun blankItem(id: Int = currentCategoryId, categories: List<Category>? = null): List<LibraryItem> {
        val actualCategories = categories ?: this.categories
        return listOf(
            LibraryPlaceholderItem.blank(
                id,
                LibraryHeaderItem({ actualCategories.getOrDefault(id) }, id),
                viewContext,
            ),
        )
    }

    fun restoreLibrary() {
        val show = showAllCategories || !libraryIsGrouped || categories.size == 1
        if (!show && currentCategoryId == -1) {
            currentCategory = categories.find { it.order == preferences.lastUsedCategory().get() }
        }
        view?.onNextLibraryUpdate(
            if (!show) {
                libraryToDisplay[currentCategory]
                    ?: libraryToDisplay[categories.first()]
                    ?: blankItem()
            } else {
                libraryItemsToDisplay
            },
            true,
        )
    }

    fun getAnimeInCategories(catId: Int?): List<LibraryAnime>? {
        catId ?: return null
        return currentLibraryItems
            .filterIsInstance<LibraryAnimeItem>()
            .filter { it.header.category.id == catId }
            .map { it.anime }
    }

    private suspend fun sectionLibrary(items: LibraryMap, freshStart: Boolean = false) {
        val showAll = showAllCategories || !libraryIsGrouped || categories.size <= 1

        libraryToDisplay = items.toMutableMap()

        if (!showAll && currentCategoryId == -1) {
            currentCategory = categories.find { it.order == preferences.lastUsedCategory().get() }
        }

        withUIContext {
            view?.onNextLibraryUpdate(
                if (!showAll) {
                    libraryToDisplay[currentCategory]
                        ?: libraryToDisplay[categories.first()]
                        ?: blankItem()
                } else {
                    libraryItemsToDisplay
                },
                freshStart,
            )
        }
    }

    /**
     * Applies library filters to the given list of anime.
     *
     * @param items the items to filter.
     */
    private suspend fun LibraryMap.applyFilters(): LibraryMap {
        val filterPrefs = getPreferencesFlow().first()
        val showEmptyCategoriesWhileFiltering = preferences.showEmptyCategoriesWhileFiltering().get()

        val filterTrackers = FilterBottomSheet.FILTER_TRACKER

        val filtersOff = view?.isSubClass != true &&
            (
                filterPrefs.filterDownloaded == 0 &&
                filterPrefs.filterUnread == 0 &&
                filterPrefs.filterCompleted == 0 &&
                filterPrefs.filterTracked == 0 &&
                filterPrefs.filterAnimeType == 0 &&
                filterPrefs.filterContentType == 0
            )
        hasActiveFilters = !filtersOff
        val realCount = mutableMapOf<Int, Int>()
        val filteredItems = this.mapValues { (key, items) ->
            if (showEmptyCategoriesWhileFiltering) {
                realCount[key.id ?: 0] = libraryToDisplay[key]?.size ?: 0
            }

            items.filter f@{ item ->
                if (item is LibraryAnimeItem) {
                    return@f matchesFilters(
                        item,
                        filterPrefs,
                        filterTrackers,
                    )
                }

                if (
                    !showEmptyCategoriesWhileFiltering
                    && item is LibraryPlaceholderItem
                    && item.type is LibraryPlaceholderItem.Type.Hidden
                ) {
                    val subItems = (libraryToDisplay[key] ?: hiddenLibraryItems)
                            .filterIsInstance<LibraryAnimeItem>()
                            .filter { it.anime.category == item.category }
                    if (subItems.isEmpty()) {
                        return@f filtersOff
                    } else {
                        return@f subItems.any {
                            matchesFilters(
                                it,
                                filterPrefs,
                                filterTrackers,
                            )
                        }
                    }
                }

                if (showAllCategories) {
                    filtersOff || showEmptyCategoriesWhileFiltering
                } else {
                    true
                }
            }.ifEmpty {
                if (showEmptyCategoriesWhileFiltering) {
                    val catId = key.id!!
                    listOf(
                        LibraryPlaceholderItem.blank(
                            catId,
                            LibraryHeaderItem({ this@AnimeLibraryPresenter.categories.getOrDefault(catId) }, catId),
                            viewContext,
                            realCount[catId] ?: 0,
                        ),
                    )
                } else {
                    emptyList()
                }
            }
        }.toMutableMap()
        return filteredItems
    }

    private suspend fun matchesFilters(
        item: LibraryAnimeItem,
        filterPrefs: ItemPreferences,
        filterTrackers: String,
    ): Boolean {
        (view as? FilteredLibraryController)?.let {
            return matchesCustomFilters(item, it, filterTrackers)
        }

        if (filterPrefs.filterUnread == STATE_INCLUDE && item.anime.unseen == 0) return false
        if (filterPrefs.filterUnread == STATE_EXCLUDE && item.anime.unseen > 0) return false

        // Filter for unseen episodes
        if (filterPrefs.filterUnread == 3 && !(item.anime.unseen > 0 && !item.anime.hasSeen)) return false
        if (filterPrefs.filterUnread == 4 && !(item.anime.unseen > 0 && item.anime.hasSeen)) return false

        if (filterPrefs.filterBookmarked == STATE_INCLUDE && item.anime.bookmarkCount == 0) return false
        if (filterPrefs.filterBookmarked == STATE_EXCLUDE && item.anime.bookmarkCount > 0) return false

        if (filterPrefs.filterAnimeType > 0) {
            if (if (filterPrefs.filterAnimeType == Anime.TYPE_MANHWA) {
                item.anime.anime.seriesType(animeSourceManager = animeSourceManager) !in arrayOf(filterPrefs.filterAnimeType, Anime.TYPE_WEBTOON)
            } else {
                    filterPrefs.filterAnimeType != item.anime.anime.seriesType(animeSourceManager = animeSourceManager)
                }
            ) {
                return false
            }
        }

        // Filter for completed status of anime
        if (filterPrefs.filterCompleted == STATE_INCLUDE && item.anime.anime.status != SAnime.COMPLETED) return false
        if (filterPrefs.filterCompleted == STATE_EXCLUDE && item.anime.anime.status == SAnime.COMPLETED) return false

        if (!matchesFilterTracking(item, filterPrefs.filterTracked, filterTrackers)) return false

        // Filter for downloaded anime
        if (filterPrefs.filterDownloaded != STATE_IGNORE) {
            val isDownloaded = when {
                item.anime.anime.isLocal() -> true
                item.downloadCount != -1 -> item.downloadCount > 0
                else -> animeDownloadManager.getDownloadCount(item.anime.anime) > 0
            }
            return if (filterPrefs.filterDownloaded == STATE_INCLUDE) isDownloaded else !isDownloaded
        }

        // Filter for NSFW/SFW contents
        if (filterPrefs.filterContentType == STATE_INCLUDE) return !item.anime.anime.isLewd()
        if (filterPrefs.filterContentType == STATE_EXCLUDE) return item.anime.anime.isLewd()
        return true
    }

    private suspend fun matchesCustomFilters(
        item: LibraryAnimeItem,
        customFilters: FilteredLibraryController,
        filterTrackers: String,
    ): Boolean {
        val statuses = customFilters.filterStatus
        if (statuses.isNotEmpty()) {
            if (item.anime.anime.status !in statuses) return false
        }
        val seriesTypes = customFilters.filterAnimeType
        if (seriesTypes.isNotEmpty()) {
            if (item.anime.anime.seriesType(animeSourceManager = animeSourceManager) !in seriesTypes) return false
        }
        val languages = customFilters.filterLanguages
        if (languages.isNotEmpty()) {
            if (getLanguage(item.anime.anime) !in languages) return false
        }
        val sources = customFilters.filterSources
        if (sources.isNotEmpty()) {
            if (item.anime.anime.source !in sources) return false
        }
        val trackingScore = customFilters.filterTrackingScore
        if (trackingScore > 0 || trackingScore == -1) {
            val tracks = getTrack.awaitAllByAnimeId(item.anime.anime.id!!)

            val hasTrack = loggedServices.any { service ->
                tracks.any { it.sync_id == service.id }
            }
            if (trackingScore > 0 && !hasTrack) return false

            if (getMeanScoreToInt(tracks) != trackingScore) return false
        }
        if (!matchesFilterTracking(item, customFilters.filterTracked, filterTrackers)) return false
        val startingYear = customFilters.filterStartYear
        if (startingYear > 0) {
            val animeStartingYear = item.anime.getStartYear()
            if (animeStartingYear != startingYear) return false
        }
        val animeLength = customFilters.filterLength
        if (animeLength != null) {
            if (item.anime.totalEpisodes !in animeLength) return false
        }
        val categories = customFilters.filterCategories
        if (categories.isNotEmpty()) {
            if (item.anime.category !in categories) return false
        }
        val tags = customFilters.filterTags
        if (tags.isNotEmpty()) {
            val genres = item.anime.anime.getGenres() ?: return false
            if (tags.none { tag -> genres.any { it.equals(tag, true) } }) return false
        }
        return true
    }

    /**
     * Get mean score rounded to int of a single anime
     */
    private fun getMeanScoreToInt(tracks: List<Track>): Int {
        val scoresList = tracks.filter { it.score > 0 }
            .mapNotNull { it.get10PointScore() }
        return if (scoresList.isEmpty()) -1 else scoresList.average().roundToInt().coerceIn(1..10)
    }

    private suspend fun LibraryAnime.getStartYear(): Int {
        if (getEpisode.awaitAll(anime.id!!, false).any { it.seen }) {
            val episodes = getHistory.awaitAllByAnimeId(anime.id!!).filter { it.last_seen > 0 }
            val date = episodes.minOfOrNull { it.last_seen } ?: return -1
            val cal = Calendar.getInstance().apply { timeInMillis = date }
            return if (date <= 0L) -1 else cal.get(Calendar.YEAR)
        }
        return -1
    }

    /**
     * Convert the score to a 10 point score
     */
    private fun Track.get10PointScore(): Float? {
        val service = trackManager.getService(this.sync_id)
        return service?.get10PointScore(this.score)
    }

    private suspend fun matchesFilterTracking(
        item: LibraryAnimeItem,
        filterTracked: Int,
        filterTrackers: String,
    ): Boolean {
        // Filter for tracked (or per tracked service)
        if (filterTracked != STATE_IGNORE) {
            val tracks = getTrack.awaitAllByAnimeId(item.anime.anime.id!!)

            val hasTrack = loggedServices.any { service ->
                tracks.any { it.sync_id == service.id }
            }
            val service = if (filterTrackers.isNotEmpty()) {
                loggedServices.find {
                    context.getString(it.nameRes()) == filterTrackers
                }
            } else {
                null
            }
            if (filterTracked == STATE_INCLUDE) {
                if (!hasTrack) return false
                if (filterTrackers.isNotEmpty()) {
                    if (service != null) {
                        val hasServiceTrack = tracks.any { it.sync_id == service.id }
                        if (!hasServiceTrack) return false
                        if (filterTracked == STATE_EXCLUDE) return false
                    }
                }
            } else if (filterTracked == STATE_EXCLUDE) {
                if (hasTrack && filterTrackers.isEmpty()) return false
                if (filterTrackers.isNotEmpty()) {
                    if (service != null) {
                        val hasServiceTrack = tracks.any { it.sync_id == service.id }
                        if (hasServiceTrack) return false
                    }
                }
            }
        }
        return true
    }

    /**
     * Sets downloaded episode count to each anime.
     *
     * @param itemList the map of anime.
     */
    private fun setDownloadCount(itemList: List<LibraryItem>) {
        if (!preferences.downloadBadge().get()) {
            // Unset download count if the preference is not enabled.
            for (item in itemList) {
                if (item !is LibraryAnimeItem) continue
                item.downloadCount = -1
            }
            return
        }

        for (item in itemList) {
            if (item !is LibraryAnimeItem) continue
            item.downloadCount = animeDownloadManager.getDownloadCount(item.anime.anime)
        }
    }

    private fun setUnreadBadge(itemList: List<LibraryItem>) {
        val unseenType = preferences.unseenBadgeType().get()
        for (item in itemList) {
            if (item !is LibraryAnimeItem) continue
            item.unseenType = unseenType
        }
    }

    private fun setSourceLanguage(itemList: List<LibraryItem>) {
        val showLanguageBadges = preferences.languageBadge().get()
        for (item in itemList) {
            if (item !is LibraryAnimeItem) continue
            item.sourceLanguage = if (showLanguageBadges) getLanguage(item.anime.anime) else null
        }
    }

    private fun getLanguage(anime: Anime): String? {
        return if (anime.isLocal()) {
            LocalAnimeSource.getAnimeLang(anime)
        } else {
            animeSourceManager.get(anime.source)?.lang
        }
    }

    private fun onCategoryUpdate(update: CategoryUpdate) {
        presenterScope.launchNonCancellableIO { updateCategories.awaitOne(update) }
    }

    /**
     * Applies library sorting to the given list of anime.
     *
     * @param itemList the map to sort.
     */
    private fun LibraryMap.applySort(): LibraryMap {
        // Making sure `allCategories` is stable for `.sort()`
        val categoryOrderMap = allCategories.associate { it.id to it.order }

        val sortFn: (LibraryItem, LibraryItem) -> Int = { i1, i2 ->
            val category = i1.header.category
            val compare = when {
                i1 is LibraryPlaceholderItem -> -1
                i2 is LibraryPlaceholderItem -> 1
                i1 !is LibraryAnimeItem || i2 !is LibraryAnimeItem -> 0
                category.animeSort != null -> {
                    var sort = when (category.sortingMode() ?: LibrarySort.Title) {
                        LibrarySort.Title -> sortAlphabetical(i1, i2)
                        LibrarySort.LatestEpisode -> i2.anime.latestUpdate.compareTo(i1.anime.latestUpdate)
                        LibrarySort.Unread -> when {
                            i1.anime.unseen == i2.anime.unseen -> 0
                            i1.anime.unseen == 0 -> if (category.isAscending()) 1 else -1
                            i2.anime.unseen == 0 -> if (category.isAscending()) -1 else 1
                            else -> i1.anime.unseen.compareTo(i2.anime.unseen)
                        }
                        LibrarySort.LastSeen -> {
                            i2.anime.lastSeen.compareTo(i1.anime.lastSeen)
                        }
                        LibrarySort.TotalEpisodes -> {
                            i2.anime.totalEpisodes.compareTo(i1.anime.totalEpisodes)
                        }
                        LibrarySort.DateFetched -> {
                            i2.anime.lastFetch.compareTo(i1.anime.lastFetch)
                        }
                        LibrarySort.DateAdded -> i2.anime.anime.date_added.compareTo(i1.anime.anime.date_added)
                        LibrarySort.DragAndDrop -> {
                            if (category.isDynamic) {
                                val category1 = categoryOrderMap[i1.anime.category] ?: 0
                                val category2 = categoryOrderMap[i2.anime.category] ?: 0
                                category1.compareTo(category2)
                            } else {
                                sortAlphabetical(i1, i2)
                            }
                        }
                        LibrarySort.Random -> {
                            error("You're not supposed to be here...")
                        }
                    }
                    if (!category.isAscending()) sort *= -1
                    sort
                }
                category.animeOrder.isNotEmpty() -> {
                    val order = category.animeOrder
                    val index1 = order.indexOf(i1.anime.anime.id!!)
                    val index2 = order.indexOf(i2.anime.anime.id!!)
                    when {
                        index1 == index2 -> 0
                        index1 == -1 -> -1
                        index2 == -1 -> 1
                        else -> index1.compareTo(index2)
                    }
                }
                else -> 0
            }
            if (compare == 0 && i1 is LibraryAnimeItem && i2 is LibraryAnimeItem) {
                sortAlphabetical(i1, i2)
            } else {
                compare
            }
        }

        return this.mapValues { (category, values) ->
            // Making sure category has valid sort before doing the actual sorting
            if (category.animeOrder.isEmpty() && category.animeSort == null) {
                category.changeSortTo(preferences.librarySortingMode().get())
                if (category.id == 0) {
                    preferences.defaultAnimeOrder()
                        .set(category.animeSort.toString())
                } else if (!category.isDynamic) {
                    onCategoryUpdate(
                        CategoryUpdate(
                            id = category.id!!.toLong(),
                            animeOrder = category.animeOrderToString(),
                        )
                    )
                }
            }

            if (LibrarySort.valueOf(category.animeSort) == LibrarySort.Random) {
                return@mapValues values
                    .asSequence()
                    .shuffled(Random(libraryPreferences.randomSortSeed().get()))
                    .sortedWith { i1, i2 ->
                        when {
                            i1 is LibraryPlaceholderItem -> -1
                            i2 is LibraryPlaceholderItem -> 1
                            else -> 0
                        }
                    }
                    .toList()
            }

            values.sortedWith(Comparator(sortFn))
        }.toSortedMap { category, category2 ->
            when {
                // Force default category to always be at the top. This also for some reason fixed a bug where Default
                // category would disappear whenever a new category is added.
                category.id == 0 -> -1
                category2.id == 0 -> 1
                else -> category.order.compareTo(category2.order)
            }
        }
    }

    /** Gets the category by id
     *
     * @param categoryId id of the category to get
     */
    private fun List<Category>.getOrDefault(categoryId: Int): Category {
        val category = this.find { categoryId == it.id } ?: createDefaultCategory()
        category.isAlone = this.size <= 1
        return category
    }

    /**
     * Sort 2 anime by the their title (and remove articles if need be)
     *
     * @param i1 the first anime
     * @param i2 the second anime to compare
     */
    private fun sortAlphabetical(i1: LibraryAnimeItem, i2: LibraryAnimeItem): Int {
        return if (removeArticles) {
            i1.anime.anime.title.removeArticles().compareTo(i2.anime.anime.title.removeArticles(), true)
        } else {
            i1.anime.anime.title.compareTo(i2.anime.anime.title, true)
        }
    }

    private fun getPreferencesFlow() = combine(
        preferences.filterDownloaded().changes(),
        preferences.filterUnread().changes(),
        preferences.filterCompleted().changes(),
        preferences.filterTracked().changes(),
        preferences.filterAnimeType().changes(),
        preferences.filterContentType().changes(),
        preferences.filterBookmarked().changes(),

        preferences.groupLibraryBy().changes(),
        preferences.showAllCategories().changes(),

        preferences.librarySortingMode().changes(),
        preferences.librarySortingAscending().changes(),

        preferences.collapsedCategories().changes(),
        preferences.collapsedDynamicCategories().changes(),
    ) { 
       ItemPreferences(
           filterDownloaded = it[0] as Int,
           filterUnread = it[1] as Int,
           filterCompleted = it[2] as Int,
           filterTracked = it[3] as Int,
           filterAnimeType = it[4] as Int,
           filterContentType = it[5] as Int,
           filterBookmarked = it[6] as Int,
           groupType = it[7] as Int,
           showAllCategories = it[8] as Boolean,
           sortingMode = it[9] as Int,
           sortAscending = it[10] as Boolean,
           collapsedCategories = it[11] as Set<String>,
           collapsedDynamicCategories = it[12] as Set<String>,
       )
    }

    /**
     * Library's flow.
     *
     * If category id '-1' is not empty, it means the library not grouped by categories
     */
    private fun getLibraryFlow(): Flow<LibraryData> {
        val libraryFlow = combine(
            getCategories.subscribe(),
            // FIXME: Remove retry once a real solution is found
            getLibraryAnime.subscribe().retry(1) { e -> e is NullPointerException },
            getPreferencesFlow(),
            forceUpdateEvent.receiveAsFlow(),
        ) { dbCategories, libraryAnimeList, prefs, _ ->
            groupType = prefs.groupType

            val defaultCategory = createDefaultCategory()

            // FIXME: Should return Map<Int, LibraryItem> where Int is category id
            if (groupType <= BY_DEFAULT || !libraryIsGrouped) {
                getLibraryItems(
                    dbCategories,
                    libraryAnimeList,
                    prefs.sortingMode,
                    prefs.sortAscending,
                    prefs.showAllCategories,
                    prefs.collapsedCategories,
                    defaultCategory,
                )
            } else {
                getDynamicLibraryItems(
                    libraryAnimeList,
                    prefs.sortingMode,
                    prefs.sortAscending,
                    groupType,
                    prefs.collapsedDynamicCategories,
                )
            } to listOf(defaultCategory) + dbCategories
        }

        return combine(
            libraryFlow,
            preferences.removeArticles().changes(),
        ) { library, removeArticles ->
            val (libraryItems, allCategories) = library
            val (items, categories, hiddenItems) = libraryItems

            LibraryData(
                categories = categories,
                allCategories = allCategories,
                items = items,
                hiddenItems = hiddenItems,
                removeArticles = removeArticles,
            )
        }
    }

    private fun getLibraryItems(
        dbCategories: List<Category>,
        libraryAnime: List<LibraryAnime>,
        sortingMode: Int,
        isAscending: Boolean,
        showAll: Boolean,
        collapsedCategories: Set<String>,
        defaultCategory: Category,
    ): Triple<LibraryMap, List<Category>, List<LibraryItem>> {
        val categories = dbCategories.mapNotNull { if (it.id == null) null else it }.toMutableList()
        val hiddenItems = mutableListOf<LibraryItem>()

        val categoryAll = Category.createAll(
            context,
            sortingMode,
            isAscending,
        )
        val catItemAll = LibraryHeaderItem({ categoryAll }, -1)

        // NOTE: Don't call header.category, only header.catId
        val headerItems = (
            categories.map { category ->
                val id = category.id!!
                id to LibraryHeaderItem({ this@AnimeLibraryPresenter.categories.getOrDefault(id) }, id)
            } + (0 to LibraryHeaderItem({ this@AnimeLibraryPresenter.categories.getOrDefault(0) }, 0))
        ).toMap()

        val categoriesHidden = if (forceShowAllCategories || controllerIsSubClass) {
            emptySet()
        } else {
            collapsedCategories.mapNotNull { it.toIntOrNull() }.toSet()
        }

        val map = if (!libraryIsGrouped)
            libraryAnime
                .asSequence()
                .distinctBy { it.anime.id }
                .map { LibraryAnimeItem(it, catItemAll, viewContext) }
                .groupBy { categoryAll }
        else {
            val rt = libraryAnime
                .asSequence()
                .mapNotNull {
                    val headerItem = headerItems[it.category] ?: return@mapNotNull null
                    LibraryAnimeItem(it, headerItem, viewContext)
                }
                .groupBy { it.header.catId }

            // Only show default category when needed
            if (rt.containsKey(0)) categories.add(0, defaultCategory)

            // NOTE: Empty list means hide the category entirely
            categories
                .associateWith { rt[it.id].orEmpty() }
                .mapValues { (key, values) ->
                    val catId = key.id!!
                    val headerItem = headerItems[catId]!!
                    if (key.isHidden) {
                        val mergedTitle = values.joinToString("-") {
                            it.anime.title + "-" + it.anime.anime.author
                        }
                        libraryToDisplay[key] = values
                        hiddenItems.addAll(values)
                        if (headerItem != null) {
                            return@mapValues listOf(
                                LibraryPlaceholderItem.hidden(
                                    catId,
                                    headerItem,
                                    viewContext,
                                    mergedTitle,
                                    values,
                                ),
                            )
                        }
                    }

                    // Making sure empty category is shown properly
                    values.ifEmpty {
                        listOf(
                            LibraryPlaceholderItem.blank(
                                catId,
                                headerItem,
                                viewContext,
                            ),
                        )
                    }
                }
        }.toMutableMap()

        categories.forEach { it.isHidden = it.id in categoriesHidden && showAll && categories.size > 1 }

        return Triple(
            map,
            if (!libraryIsGrouped) {
                arrayListOf(categoryAll)
            } else {
                categories
            },
            hiddenItems,
        )
    }

    private suspend fun getDynamicLibraryItems(
        libraryAnime: List<LibraryAnime>,
        sortingMode: Int,
        isAscending: Boolean,
        groupType: Int,
        collapsedDynamicCategories: Set<String>,
    ): Triple<LibraryMap, List<Category>, List<LibraryItem>> {
        val tagItems: MutableMap<String, LibraryHeaderItem> = mutableMapOf()
        val hiddenItems = mutableListOf<LibraryItem>()

        // internal function to make headers
        fun makeOrGetHeader(name: String, checkNameSwap: Boolean = false): LibraryHeaderItem {
            tagItems[name]?.let { return it }
            if (checkNameSwap && name.contains(" ")) {
                val swappedName = name.split(" ").reversed().joinToString(" ")
                if (tagItems.containsKey(swappedName)) {
                    return tagItems[swappedName]!!
                }
            }
            val headerItem = LibraryHeaderItem({ categories.getOrDefault(it) }, tagItems.count())
            tagItems[name] = headerItem
            return headerItem
        }

        val hiddenDynamics = if (controllerIsSubClass) {
            emptySet()
        } else {
            collapsedDynamicCategories
        }

        val unknown = context.getString(MR.strings.unknown)
        val items = libraryAnime.distinctBy { it.anime.id }.map { anime ->
            when (groupType) {
                BY_TAG -> {
                    val tags = if (anime.anime.genre.isNullOrBlank()) {
                        listOf(unknown)
                    } else {
                        anime.anime.genre?.split(",")?.mapNotNull {
                            val tag = it.trim().capitalizeWords()
                            tag.ifBlank { null }
                        } ?: listOf(unknown)
                    }
                    tags.map {
                        LibraryAnimeItem(anime, makeOrGetHeader(it), viewContext)
                    }
                }
                BY_TRACK_STATUS -> {
                    val tracks = getTrack.awaitAllByAnimeId(anime.anime.id!!)
                    val track = tracks.find { track ->
                        loggedServices.any { it.id == track.sync_id }
                    }
                    val service = loggedServices.find { it.id == track?.sync_id }
                    val status: String = if (track != null && service != null) {
                        if (loggedServices.size > 1) {
                            service.getGlobalStatus(track.status)
                        } else {
                            service.getStatus(track.status)
                        }
                    } else {
                        view?.view?.context?.getString(MR.strings.not_tracked) ?: ""
                    }
                    listOf(LibraryAnimeItem(anime, makeOrGetHeader(status), viewContext))
                }
                BY_SOURCE -> {
                    val source = animeSourceManager.getOrStub(anime.anime.source)
                    listOf(
                        LibraryAnimeItem(
                            anime,
                            makeOrGetHeader("${source.name}$sourceSplitter${source.id}"),
                            viewContext,
                        ),
                    )
                }
                BY_AUTHOR -> {
                    if (anime.anime.artist.isNullOrBlank() && anime.anime.author.isNullOrBlank()) {
                        listOf(LibraryAnimeItem(anime, makeOrGetHeader(unknown), viewContext))
                    } else {
                        listOfNotNull(
                            anime.anime.author.takeUnless { it.isNullOrBlank() },
                            anime.anime.artist.takeUnless { it.isNullOrBlank() },
                        ).map {
                            it.split(",", "/", " x ", " - ", ignoreCase = true).mapNotNull {
                                val author = it.trim()
                                author.ifBlank { null }
                            }
                        }.flatten().distinct().map {
                            LibraryAnimeItem(anime, makeOrGetHeader(it, true), viewContext)
                        }
                    }
                }
                BY_LANGUAGE -> {
                    val lang = getLanguage(anime.anime)
                    listOf(
                        LibraryAnimeItem(
                            anime,
                            makeOrGetHeader(
                                lang?.plus(langSplitter)?.plus(
                                    run {
                                        val locale = Locale.forLanguageTag(lang)
                                        locale.getDisplayName(locale)
                                            .replaceFirstChar { it.uppercase(locale) }
                                    },
                                ) ?: unknown,
                            ),
                            viewContext,
                        ),
                    )
                }
                // BY_STATUS
                else -> listOf(LibraryAnimeItem(anime, makeOrGetHeader(context.mapStatus(anime.anime.status)), viewContext))
            }
        }.flatten().groupBy { it.header.catId }

        val headers = tagItems.map {
            Category.createCustom(
                it.key,
                sortingMode,
                isAscending,
            ).apply {
                id = item.value.catId
                if (name.contains(sourceSplitter)) {
                    val split = name.split(sourceSplitter)
                    name = split.first()
                    sourceId = split.last().toLongOrNull()
                } else if (name.contains(langSplitter)) {
                    val split = name.split(langSplitter)
                    name = split.last()
                    langId = split.first()
                }
                isHidden = getDynamicCategoryName(this) in hiddenDynamics
            }
        }.sortedWith(
            compareBy(String.CASE_INSENSITIVE_ORDER) {
                if (groupType == BY_TRACK_STATUS) {
                    mapTrackingOrder(it.name)
                } else {
                    it.name
                }
            },
        ).let { headers ->
            if (!preferences.collapsedDynamicAtBottom().get()) return@let headers
            headers.filterNot { it.isHidden } + headers.filter { it.isHidden }
        }

        val map = headers
            .associateWith { items[it.id].orEmpty() }
            .mapValues {
                val catId = key.id!!  // null check already handled by mapNotNull
                val headerItem = tagItems[key.dynamicHeaderKey()]
                if (key.isHidden) {
                    val mergedTitle = values.joinToString("-") {
                        it.anime.title + "-" + it.anime.anime.author
                    }
                    libraryToDisplay[key] = values
                    hiddenItems.addAll(values)
                    if (headerItem != null) {
                        return@mapValues listOf(
                            LibraryPlaceholderItem.hidden(
                                catId,
                                headerItem,
                                viewContext,
                                mergedTitle,
                                values,
                            ),
                        )
                    }
                }
                values
            }

        headers.forEachIndexed { index, category -> category.order = index }
        return Triple(map, headers, hiddenItems)
    }

    private fun mapTrackingOrder(status: String): String {
        with(context) {
            return when (status) {
                getString(MR.strings.reading), getString(MR.strings.currently_reading) -> "1"
                getString(MR.strings.rereading) -> "2"
                getString(MR.strings.plan_to_read), getString(MR.strings.want_to_read) -> "3"
                getString(MR.strings.on_hold), getString(MR.strings.paused) -> "4"
                getString(MR.strings.completed) -> "5"
                getString(MR.strings.dropped) -> "6"
                else -> "7"
            }
        }
    }

    /** Create a default category with the sort set */
    private fun createDefaultCategory(): Category {
        val default = Category.createDefault(view?.applicationContext ?: context)
        default.order = -1
        val defOrder = preferences.defaultAnimeOrder().get()
        if (defOrder.firstOrNull()?.isLetter() == true) {
            default.animeSort = defOrder.first()
        } else {
            default.animeOrder = defOrder.split("/").mapNotNull { it.toLongOrNull() }
        }
        return default
    }

    /** Requests the library to be filtered. */
    fun requestFilterUpdate() {
        presenterScope.launch {
            val animeMap = currentLibrary
                .applyFilters()
                .applySort()
            sectionLibrary(animeMap)
        }
    }

    private fun requestBadgeUpdate(badgeUpdate: (List<LibraryItem>) -> Unit) {
        presenterScope.launch {
            val animeMap = currentLibrary
            animeMap.forEach { (_, items) -> badgeUpdate(items) }
            currentLibrary = animeMap
            val current = libraryToDisplay
            current.forEach { (_, items) -> badgeUpdate(items) }
            sectionLibrary(current)
        }
    }

    /** Requests the library to have download badges added/removed. */
    fun requestDownloadBadgesUpdate() {
        requestBadgeUpdate { setDownloadCount(it) }
    }

    /** Requests the library to have unseen badges changed. */
    fun requestUnseenBadgesUpdate() {
        requestBadgeUpdate { setUnreadBadge(it) }
    }

    /** Requests the library to have language badges changed. */
    fun requestLanguageBadgesUpdate() {
        requestBadgeUpdate { setSourceLanguage(it) }
    }

    /** Requests the library to be sorted. */
    private fun requestSortUpdate() {
        presenterScope.launch {
            val animeMap = libraryToDisplay
                .applySort()
            sectionLibrary(animeMap)
        }
    }

    fun getAnimeUrls(animes: List<Anime>): List<String> {
        return animes.mapNotNull { anime ->
            val source = animeSourceManager.get(anime.source) as? AnimeHttpSource ?: return@mapNotNull null
            source.getAnimeUrl(anime)
        }
    }

    /**
     * Remove the selected anime from the library.
     *
     * @param animes the list of anime to delete.
     */
    fun removeAnimeFromLibrary(animes: List<Anime>) {
        presenterScope.launch {
            // Create a set of the list
            val animeToDelete = animes.distinctBy { it.id }
                .mapNotNull { if (it.id != null) AnimeUpdate(it.id!!, favorite = false) else null }

            withIOContext { updateAnime.awaitAll(animeToDelete) }
        }
    }

    /** Remove anime from the library and delete the downloads */
    fun confirmDeletion(animes: List<Anime>, coverCacheToo: Boolean = true) {
        presenterScope.launchNonCancellableIO {
            val animeToDelete = animes.distinctBy { it.id }
            animeToDelete.forEach { anime ->
                if (coverCacheToo) {
                    anime.removeCover(coverCache)
                }
                val source = animeSourceManager.get(anime.source) as? AnimeHttpSource
                if (source != null) {
                    animeDownloadManager.deleteAnime(anime, source)
                }
            }
            if (!coverCacheToo) {
                requestDownloadBadgesUpdate()
            }
        }
    }

    /** Force update the library */
    fun updateLibrary() = presenterScope.launch {
        forceUpdateEvent.send(Unit)
    }


    /** Undo the removal of the anime once in library */
    fun reAddAnimes(animes: List<Anime>) {
        presenterScope.launch {
            val animeToAdd = animes.distinctBy { it.id }
                .mapNotNull { if (it.id != null) AnimeUpdate(it.id!!, favorite = true) else null }

            withIOContext { updateAnime.awaitAll(animeToAdd) }
            (view as? FilteredLibraryController)?.updateStatsPage()
        }
    }

    /** Returns first unseen episode of an anime */
    fun getFirstUnseen(anime: Anime): Episode? {
        // FIXME: Don't do blocking
        val episodes = runBlocking { getEpisode.awaitAll(anime) }
        return EpisodeSort(anime, episodeFilter, preferences).getNextUnseenEpisode(episodes, false)
    }

    /** Update a category's sorting */
    fun sortCategory(catId: Int, order: Char) {
        val category = categories.find { catId == it.id } ?: return
        category.animeSort = order
        if (catId == -1 || category.isDynamic) {
            val sort = category.sortingMode() ?: LibrarySort.Title
            preferences.librarySortingMode().set(sort.mainValue)
            preferences.librarySortingAscending().set(category.isAscending())
            categories.forEach {
                it.animeSort = category.animeSort
            }
        } else if (catId >= 0) {
            if (category.id == 0) {
                preferences.defaultAnimeOrder().set(category.animeSort.toString())
            } else {
                onCategoryUpdate(
                    CategoryUpdate(
                        id = catId.toLong(),
                        animeOrder = category.animeOrderToString(),
                    ),
                )
            }
        }
        requestSortUpdate()
    }

    /** Update a category's order */
    fun rearrangeCategory(catId: Int?, animeIds: List<Long>) {
        presenterScope.launch {
            val category = categories.find { catId == it.id } ?: return@launch
            if (category.isDynamic) return@launch
            category.animeSort = null
            category.animeOrder = animeIds
            if (category.id == 0) {
                preferences.defaultAnimeOrder().set(animeIds.joinToString("/"))
            } else {
                updateCategories.awaitOne(
                    CategoryUpdate(
                        id = category.id!!.toLong(),
                        animeOrder = category.animeOrderToString(),
                    ),
                )
            }
            requestSortUpdate()
        }
    }

    /** Shift an anime's category via drag & drop */
    fun moveAnimeToCategory(
        anime: LibraryAnime,
        catId: Int?,
        animeIds: List<Long>,
    ) {
        presenterScope.launch {
            val categoryId = catId ?: return@launch
            val category = categories.find { catId == it.id } ?: return@launch
            if (category.isDynamic) return@launch

            val oldCatId = anime.category
            anime.category = categoryId

            val mc = ArrayList<Long>()
            val categories =
                if (catId == 0) {
                    emptyList()
                } else {
                    getCategories.awaitByAnimeId(anime.anime.id!!)
                        .filter { it.id != oldCatId } + listOf(category)
                }

            for (cat in categories) {
                mc.add(cat.id!!.toLong())
            }

            setAnimeCategories.await(anime.anime.id!!, mc)

            if (category.animeSort == null) {
                val ids = animeIds.toMutableList()
                if (!ids.contains(anime.anime.id!!)) ids.add(anime.anime.id!!)
                category.animeOrder = ids
                if (category.id == 0) {
                    preferences.defaultAnimeOrder()
                        .set(animeIds.joinToString("/"))
                } else {
                    updateCategories.awaitOne(
                        CategoryUpdate(
                            id = category.id!!.toLong(),
                            animeOrder = category.animeOrderToString(),
                        ),
                    )
                }
            }
            updateLibrary()
        }
    }

    /** Returns if anime is in a category by id */
    fun animeIsInCategory(anime: LibraryAnime, catId: Int?): Boolean {
        // FIXME: Don't do blocking
        val categories = runBlocking { getCategories.awaitByAnimeId(anime.anime.id!!) }.map { it.id }
        return catId in categories
    }

    fun toggleCategoryVisibility(categoryId: Int) {
        // if (categories.find { it.id == categoryId }?.isDynamic == true) return
        if (groupType == BY_DEFAULT) {
            val categoriesHidden = preferences.collapsedCategories().get().mapNotNull {
                it.toIntOrNull()
            }.toMutableSet()
            if (categoryId in categoriesHidden) {
                categoriesHidden.remove(categoryId)
            } else {
                categoriesHidden.add(categoryId)
            }
            preferences.collapsedCategories(
                ).set(categoriesHidden.map { it.toString() }.toMutableSet())
        } else {
            val categoriesHidden = preferences.collapsedDynamicCategories().get().toMutableSet()
            val category = categories.getOrDefault(categoryId)
            val dynamicName = getDynamicCategoryName(category)
            if (dynamicName in categoriesHidden) {
                categoriesHidden.remove(dynamicName)
            } else {
                categoriesHidden.add(dynamicName)
            }
            preferences.collapsedDynamicCategories().set(categoriesHidden)
        }
    }

    private fun getDynamicCategoryName(category: Category): String =
        groupType.toString() + dynamicCategorySplitter + (
            category.sourceId?.toString() ?: category.langId ?: category.name
            )

    fun toggleAllCategoryVisibility() {
        if (groupType == BY_DEFAULT) {
            if (allCategoriesExpanded()) {
                preferences.collapsedCategories(
                ).set(allCategories.map { it.id.toString() }.toMutableSet())
            } else {
                preferences.collapsedCategories().set(mutableSetOf())
            }
        } else {
            if (allCategoriesExpanded()) {
                preferences.collapsedDynamicCategories() += categories.map {
                    getDynamicCategoryName(
                        it,
                    )
                }
            } else {
                preferences.collapsedDynamicCategories() -= categories.map {
                    getDynamicCategoryName(
                        it,
                    )
                }
            }
        }
    }

    fun allCategoriesExpanded(): Boolean {
        return if (groupType == BY_DEFAULT) {
            preferences.collapsedCategories().get().isEmpty()
        } else {
            categories.none { it.isHidden }
        }
    }

    /** download All unseen */
    fun downloadUnseen(animeList: List<Anime>) {
        presenterScope.launch {
            withContext(Dispatchers.IO) {
                animeList.forEach { list ->
                    val episodes = getEpisode.awaitAll(list).filter { !it.seen }
                    animeDownloadManager.downloadEpisodes(list, episodes)
                }
            }
            if (preferences.downloadBadge().get()) {
                requestDownloadBadgesUpdate()
            }
        }
    }

    fun markSeenStatus(
        animeList: List<Anime>,
        markSeen: Boolean,
    ): HashMap<Anime, List<Episode>> {
        val mapAnimeEpisodes = HashMap<Anime, List<Episode>>()
        presenterScope.launchNonCancellableIO {
            animeList.forEach { anime ->
                val episodes = getEpisode.awaitAll(anime)
                val updates = episodes.copy().mapNotNull {
                    if (it.id == null) return@mapNotNull null
                    EpisodeUpdate(it.id!!, seen = markSeen, lastPageSeen = 0)
                }
                updateEpisode.awaitAll(updates)

                mapAnimeEpisodes[anime] = episodes
            }
            updateLibrary()
        }
        return mapAnimeEpisodes
    }

    fun undoMarkSeenStatus(
        animeList: HashMap<Anime, List<Episode>>,
    ) {
        presenterScope.launchNonCancellableIO {
            val updates = animeList.values.map { episodes ->
                episodes.mapNotNull {
                    if (it.id == null) return@mapNotNull null
                    EpisodeUpdate(it.id!!, seen = it.seen, lastPageSeen = it.last_page_seen.toLong())
                }
            }.flatten()
            updateEpisode.awaitAll(updates)
            updateLibrary()
        }
    }

    fun confirmMarkSeenStatus(
        animeList: HashMap<Anime, List<Episode>>,
        markSeen: Boolean,
    ) {
        if (preferences.removeAfterMarkedAsSeen().get() && markSeen) {
            animeList.forEach { (anime, oldEpisodes) ->
                deleteEpisodes(anime, oldEpisodes)
            }
            if (preferences.downloadBadge().get()) {
                requestDownloadBadgesUpdate()
            }
        }
    }

    private fun deleteEpisodes(anime: Anime, episodes: List<Episode>) {
        animeSourceManager.get(anime.source)?.let { source ->
            animeDownloadManager.deleteEpisodes(episodes, anime, source)
        }
    }

    companion object {
        private var lastDisplayedLibrary: LibraryMutableMap? = null
        private var lastCategories: List<Category>? = null
        private var lastLibrary: LibraryMap? = null
        private const val dynamicCategorySplitter = "▄╪\t▄╪\t▄"

        private val randomTags = arrayOf(0, 1, 2)
        private const val randomSource = 4
        private const val randomTitle = 3

        @Suppress("unused")
        private const val randomTag = 0
        private val randomGroupOfTags = arrayOf(1, 2)
        private const val randomGroupOfTagsNormal = 1

        @Suppress("unused")
        private const val randomGroupOfTagsNegate = 2

        fun onLowMemory() {
            lastDisplayedLibrary = null
            lastCategories = null
            lastLibrary = null
        }

        suspend fun setSearchSuggestion(
            preferences: PreferencesHelper,
            getLibraryAnime: GetLibraryAnime,
            animeSourceManager: AnimeSourceManager,
        ) {
            val random: Random = run {
                val cal = Calendar.getInstance()
                cal.time = Date()
                cal[Calendar.MINUTE] = 0
                cal[Calendar.SECOND] = 0
                cal[Calendar.MILLISECOND] = 0
                Random(cal.time.time)
            }

            preferences.librarySearchSuggestion().set(
                when (val value = random.nextInt(0, 5)) {
                    randomSource -> {
                        val distinctSources = getLibraryAnime.await().distinctBy { it.anime.source }
                        val randomSource =
                            animeSourceManager.get(
                                distinctSources.randomOrNull(random)?.anime?.source ?: 0L,
                            )?.name
                        randomSource?.chopByWords(30)
                    }
                    randomTitle -> {
                        getLibraryAnime.await().randomOrNull(random)?.anime?.title?.chopByWords(30)
                    }
                    in randomTags -> {
                        val tags = RecentsPresenter.getRecentAnime(true)
                            .map { it.first.genre.orEmpty().split(",").map(String::trim) }
                            .flatten()
                            .filter { it.isNotBlank() }
                        val distinctTags = tags.distinct()
                        if (value in randomGroupOfTags && distinctTags.size > 6) {
                            val shortestTagsSort = distinctTags.sortedBy { it.length }
                            val offset = random.nextInt(0, distinctTags.size / 2 - 2)
                            var offset2 = random.nextInt(0, distinctTags.size / 2 - 2)
                            while (offset2 == offset) {
                                offset2 = random.nextInt(0, distinctTags.size / 2 - 2)
                            }
                            if (value == randomGroupOfTagsNormal) {
                                "${shortestTagsSort[offset]}, " + shortestTagsSort[offset2]
                            } else {
                                "${shortestTagsSort[offset]}, -" + shortestTagsSort[offset2]
                            }
                        } else {
                            val group = tags.groupingBy { it }.eachCount()
                            val groupedTags = distinctTags.sortedByDescending { group[it] }
                            groupedTags.take(8).randomOrNull(random)
                        }
                    }
                    else -> ""
                } ?: "",
            )

            if (!preferences.showLibrarySearchSuggestions().isSet()) {
                preferences.showLibrarySearchSuggestions().set(true)
            }
            preferences.lastLibrarySuggestion().set(Date().time)
        }

        /** Give library anime to a date added based on min episode fetch */
        suspend fun updateDB(
            getEpisode: GetEpisode = Injekt.get(),
            getLibraryAnime: GetLibraryAnime = Injekt.get(),
            updateAnime: UpdateAnime = Injekt.get(),
        ) {
            val libraryAnime = getLibraryAnime.await()
            libraryAnime.forEach { anime ->
                if (anime.anime.id == null) return@forEach
                if (anime.anime.date_added == 0L) {
                    val episodes = getEpisode.awaitAll(anime.anime.id!!, anime.anime.filtered_scanlators?.isNotBlank() == true)
                    anime.anime.date_added = episodes.minByOrNull { it.date_fetch }?.date_fetch ?: 0L
                    updateAnime.await(AnimeUpdate(anime.anime.id!!, dateAdded = anime.anime.date_added))
                }
            }
        }

        suspend fun updateRatiosAndColors(
            getAnime: GetAnime = Injekt.get(),
        ) {
            val libraryAnime = getAnime.awaitFavorites()
            libraryAnime.forEach { anime ->
                try { withUIContext { AnimeCoverMetadata.setRatioAndColors(anime.id, anime.thumbnail_url, anime.favorite) } } catch (_: Exception) { } 
            }
            AnimeCoverMetadata.savePrefs()
        }

        suspend fun updateCustoms(
            cc: CoverCache = Injekt.get(),
            updateAnime: UpdateAnime = Injekt.get(),
        ) {
            val getLibraryAnime: GetLibraryAnime by injectLazy()
            val libraryAnime = getLibraryAnime.await()
            libraryAnime.forEach { anime ->
                if (anime.anime.id == null) return@forEach
                if (anime.anime.thumbnail_url?.startsWith("custom", ignoreCase = true) == true) {
                    val file = cc.getCoverFile(anime.anime.thumbnail_url, !anime.anime.favorite)
                    if (file != null && file.exists()) {
                        file.renameTo(cc.getCustomCoverFile(anime.anime))
                    }
                    anime.anime.thumbnail_url =
                        anime.anime.thumbnail_url!!.lowercase(Locale.ROOT).substringAfter("custom-")
                    updateAnime.await(AnimeUpdate(anime.anime.id!!, thumbnailUrl = anime.anime.thumbnail_url))
                }
            }
        }
    }

    data class ItemPreferences(
        val filterDownloaded: Int,
        val filterUnread: Int,
        val filterCompleted: Int,
        val filterTracked: Int,
        val filterAnimeType: Int,
        val filterContentType: Int,
        val filterBookmarked: Int,

        val groupType: Int,
        val showAllCategories: Boolean,

        val sortingMode: Int,
        val sortAscending: Boolean,

        val collapsedCategories: Set<String>,
        val collapsedDynamicCategories: Set<String>,
    )

    data class LibraryData(
        val categories: List<Category>,
        val allCategories: List<Category>,
        val items: LibraryMap,
        val hiddenItems: List<LibraryItem>,
        val removeArticles: Boolean,
    )
}