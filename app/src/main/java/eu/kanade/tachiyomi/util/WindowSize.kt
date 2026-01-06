package eu.kanade.tachiyomi.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import eu.kanade.tachiyomi.util.system.isTablet

@Composable
@ReadOnlyComposable
fun isTablet(): Boolean {
    return LocalConfiguration.current.isTablet()
}
