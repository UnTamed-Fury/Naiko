package eu.kanade.tachiyomi.ui.source

import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.extension.anime.AnimeExtensionManager
import eu.kanade.tachiyomi.animesource.AnimeCatalogueSource
import eu.kanade.tachiyomi.animesource.LocalAnimeSource
import eu.kanade.tachiyomi.source.anime.AnimeSourceManager
import eu.kanade.tachiyomi.util.system.withUIContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.util.TreeMap

class AnimeSourcePresenter(
    val controller: BrowseController,
    val sourceManager: AnimeSourceManager = Injekt.get(),
    val extensionManager: AnimeExtensionManager = Injekt.get(),
    private val preferences: PreferencesHelper = Injekt.get(),
) {

    private var scope = CoroutineScope(Job() + Dispatchers.Default)
    var sources = getEnabledSources()

    var sourceItems = emptyList<SourceItem>()
    var lastUsedItem: SourceItem? = null

    var lastUsedJob: Job? = null

    fun onCreate() {
        if (lastSources != null) {
            if (sourceItems.isEmpty()) {
                sourceItems = lastSources ?: emptyList()
            }
            lastUsedItem = lastUsedItemRem
            lastSources = null
            lastUsedItemRem = null
        }
        loadSources()
    }

    private fun loadSources() {
        scope.launch {
            val pinnedSources = mutableListOf<SourceItem>()
            val pinnedCatalogues = preferences.pinnedCatalogues().get()

            val map = TreeMap<String, MutableList<AnimeCatalogueSource>> { d1, d2 ->
                when {
                    d1 == "" && d2 != "" -> 1
                    d2 == "" && d1 != "" -> -1
                    else -> d1.compareTo(d2)
                }
            }
            val byLang = sources.groupByTo(map) { it.lang }
            sourceItems = byLang.flatMap {
                val langItem = LangItem(it.key)
                it.value.map { source ->
                    val isPinned = source.id.toString() in pinnedCatalogues
                    if (source.id.toString() in pinnedCatalogues) {
                        pinnedSources.add(SourceItem(source, LangItem(PINNED_KEY)))
                    }

                    SourceItem(source, langItem, isPinned)
                }
            }

            if (pinnedSources.isNotEmpty()) {
                sourceItems = pinnedSources + sourceItems
            }

            lastUsedItem = getLastUsedSource(preferences.lastUsedCatalogueSource().get())
            withUIContext {
                controller.setSources(sourceItems, lastUsedItem)
                loadLastUsedSource()
            }
        }
    }

    private fun loadLastUsedSource() {
        lastUsedJob?.cancel()
        lastUsedJob = preferences.lastUsedCatalogueSource().changes()
            .drop(1)
            .onEach {
                lastUsedItem = getLastUsedSource(it)
                withUIContext {
                    controller.setLastUsedSource(lastUsedItem)
                }
            }.launchIn(scope)
    }

    private fun getLastUsedSource(value: Long): SourceItem? {
        return (sourceManager.get(value) as? AnimeCatalogueSource)?.let { source ->
            val pinnedCatalogues = preferences.pinnedCatalogues().get()
            val isPinned = source.id.toString() in pinnedCatalogues
            if (isPinned) {
                null
            } else {
                SourceItem(source, LangItem(LAST_USED_KEY), isPinned)
            }
        }
    }

    fun updateSources() {
        sources = getEnabledSources()
        loadSources()
    }

    fun onDestroy() {
        lastSources = sourceItems
        lastUsedItemRem = lastUsedItem
    }

    private fun getEnabledSources(): List<AnimeCatalogueSource> {
        val languages = preferences.enabledLanguages().get()
        val hiddenCatalogues = preferences.hiddenSources().get()

        return sourceManager.getCatalogueSources()
            .filter { it.lang in languages || it.id == LocalAnimeSource.ID }
            .filterNot { it.id.toString() in hiddenCatalogues }
            .sortedBy { "(${it.lang}) ${it.name}" }
    }

    companion object {
        const val PINNED_KEY = "pinned"
        const val LAST_USED_KEY = "last_used"

        private var lastSources: List<SourceItem>? = null
        private var lastUsedItemRem: SourceItem? = null

        fun onLowMemory() {
            lastSources = null
            lastUsedItemRem = null
        }
    }
}