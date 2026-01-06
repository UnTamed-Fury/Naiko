package eu.kanade.tachiyomi.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.bluelinelabs.conductor.Router
import yokai.domain.DialogHostState

val <T> ProvidableCompositionLocal<T?>.currentOrThrow
    @Composable
    get(): T = this.current ?: throw RuntimeException("CompositionLocal is null")

val LocalBackPress: ProvidableCompositionLocal<(() -> Unit)?> = staticCompositionLocalOf { null }
val LocalDialogHostState: ProvidableCompositionLocal<DialogHostState?> = compositionLocalOf { null }
@Deprecated(
    message = "Scheduled for removal once Conductor is fully replaced by Voyager",
    replaceWith = ReplaceWith("LocalNavigator", "cafe.adriel.voyager.navigator.LocalNavigator"),
)
val LocalRouter: ProvidableCompositionLocal<Router?> = compositionLocalOf { null }
