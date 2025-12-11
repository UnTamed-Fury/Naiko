package eu.kanade.tachiyomi.extension

import android.content.Context
import eu.kanade.domain.source.service.SourcePreferences
import eu.kanade.tachiyomi.extension.anime.AnimeExtensionManager
import eu.kanade.tachiyomi.extension.manga.MangaExtensionManager
import eu.kanade.tachiyomi.extension.model.Extension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class NaikoExtensionManager(context: Context) {

    val anime: AnimeExtensionManager = AnimeExtensionManager(context)
    val manga: MangaExtensionManager = MangaExtensionManager(context)

    private val preferences: SourcePreferences = Injekt.get()

    private val _currentMediaType = MutableStateFlow(MediaType.MANGA)
    val currentMediaType: StateFlow<MediaType> = _currentMediaType.asStateFlow()

    fun setMediaType(type: MediaType) {
        _currentMediaType.value = type
    }

    val installedExtensionsFlow: Flow<List<Extension.Installed>> =
        currentMediaType.combine(anime.installedExtensionsFlow) { type, animeExtensions ->
            if (type == MediaType.ANIME) animeExtensions else emptyList()
        }.combine(manga.installedExtensionsFlow) { animeList, mangaExtensions ->
            if (currentMediaType.value == MediaType.MANGA) mangaExtensions else animeList
        }

    val availableExtensionsFlow: Flow<List<Extension.Available>> =
        currentMediaType.combine(anime.availableExtensionsFlow) { type, animeExtensions ->
            if (type == MediaType.ANIME) animeExtensions else emptyList()
        }.combine(manga.availableExtensionsFlow) { animeList, mangaExtensions ->
            if (currentMediaType.value == MediaType.MANGA) mangaExtensions else animeList
        }
    
    val untrustedExtensionsFlow: Flow<List<Extension.Untrusted>> =
        currentMediaType.combine(anime.untrustedExtensionsFlow) { type, animeExtensions ->
            if (type == MediaType.ANIME) animeExtensions else emptyList()
        }.combine(manga.untrustedExtensionsFlow) { animeList, mangaExtensions ->
            if (currentMediaType.value == MediaType.MANGA) mangaExtensions else animeList
        }

    suspend fun findAvailableExtensions() {
        anime.findAvailableExtensions()
        manga.findAvailableExtensions()
    }
}

enum class MediaType {
    ANIME, MANGA
}
