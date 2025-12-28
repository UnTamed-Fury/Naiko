package naiko.presentation.library

import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eu.kanade.tachiyomi.ui.library.models.LibraryItem
import naiko.presentation.AppBarType
import naiko.presentation.NaikoScaffold
import naiko.presentation.library.components.LazyLibraryGrid

@Composable
fun LibraryContent(
    modifier: Modifier = Modifier,
    items: List<LibraryItem>,
    columns: Int,
) {
    NaikoScaffold(
        onNavigationIconClicked = {},
        appBarType = AppBarType.NONE,
    ) { contentPadding ->
        LazyLibraryGrid(
            modifier = modifier,
            columns = columns,
            contentPadding = contentPadding,
        ) {
            items(
                items = items,
                contentType = { "library_grid_item" }
            ) { item ->
                when (item) {
                    is LibraryItem.Blank -> {
                        Text("Blank: ${item.mangaCount}")
                    }
                    is LibraryItem.Hidden -> {
                        Text("Hidden: ${item.title} - ${item.hiddenItems.size}")
                    }
                    is LibraryItem.Manga -> {
                        Text("Manga: ${item.libraryManga.manga.title}")
                    }
                }
            }
        }
    }
}
