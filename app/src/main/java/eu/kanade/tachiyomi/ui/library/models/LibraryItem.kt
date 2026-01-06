package eu.kanade.tachiyomi.ui.library.models

import eu.kanade.tachiyomi.data.database.models.LibraryManga

sealed interface LibraryItem {
    data class Blank(val mangaCount: Int = 0) : LibraryItem
    data class Hidden(val title: String, val hiddenItems: List<LibraryItem>) : LibraryItem
    data class Manga(
        val libraryManga: LibraryManga,
        val isLocal: Boolean = false,
        val downloadCount: Long = -1,
        val unreadCount: Long = -1,
        val language: String = "",
    ) : LibraryItem
}
