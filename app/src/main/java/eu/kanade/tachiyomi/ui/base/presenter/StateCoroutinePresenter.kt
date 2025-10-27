package eu.kanade.tachiyomi.ui.base.presenter

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Presenter that mimic [cafe.adriel.voyager.core.model.StateScreenModel] for easier migration.
 * Temporary class while we're migrating to Compose.
 */
abstract class StateCoroutinePresenter<S, C>(initialState: S) : BaseCoroutinePresenter<C>() {

    protected val mutableState: MutableStateFlow<S> = MutableStateFlow(initialState)
    val state: StateFlow<S> = mutableState.asStateFlow()
}
