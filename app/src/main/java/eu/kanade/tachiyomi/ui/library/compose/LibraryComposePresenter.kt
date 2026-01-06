package eu.kanade.tachiyomi.ui.library.compose

import eu.kanade.tachiyomi.data.database.models.Category
import eu.kanade.tachiyomi.ui.base.presenter.StateCoroutinePresenter
import eu.kanade.tachiyomi.ui.library.models.LibraryItem

typealias LibraryMap = Map<Category, List<LibraryItem>>

class LibraryComposePresenter :
    StateCoroutinePresenter<LibraryComposePresenter.State, LibraryComposeController>(State()) {

    data class State(
        var isLoading: Boolean = true,
        var library: LibraryMap = emptyMap()
    )
}
