package yokai.domain

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

typealias ComposableDialog = (@Composable () -> Unit)?
typealias ComposableDialogState = MutableState<ComposableDialog>

class DialogHostState(initial: ComposableDialog = null) : ComposableDialogState by mutableStateOf(initial) {
    val mutex = Mutex()

    fun closeDialog() {
        value = null
    }

    suspend inline fun <R> dialog(crossinline dialog: @Composable (CancellableContinuation<R>) -> Unit) = mutex.withLock {
        try {
            suspendCancellableCoroutine { cont -> value = { dialog(cont) } }
        } finally {
            closeDialog()
        }
    }
}
