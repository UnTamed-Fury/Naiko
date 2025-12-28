package naiko.presentation.settings.screen.about

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.LocalNavigator
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.util.htmlReadyLicenseContent
import dev.icerock.moko.resources.compose.stringResource
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.util.compose.LocalBackPress
import eu.kanade.tachiyomi.util.compose.currentOrThrow
import naiko.i18n.MR
import naiko.presentation.AppBarType
import naiko.presentation.NaikoScaffold
import naiko.presentation.core.pinnedAppBarScrollBehavior
import naiko.util.Screen

class AboutLicenseScreen : Screen() {
    @Composable
    override fun Content() {
        val libraries by produceLibraries(R.raw.aboutlibraries)
        val navigator = LocalNavigator.currentOrThrow
        val backPress = LocalBackPress.currentOrThrow

        NaikoScaffold(
            onNavigationIconClicked = backPress,
            title = stringResource(MR.strings.open_source_licenses),
            appBarType = AppBarType.SMALL,
            scrollBehavior = pinnedAppBarScrollBehavior(),
        ) { innerPadding ->
            LibrariesContainer(
                libraries = libraries,
                modifier = Modifier.fillMaxSize(),
                contentPadding = innerPadding,
                onLibraryClick = {
                    navigator.push(
                        AboutLibraryLicenseScreen(
                            it.name,
                            it.website,
                            it.licenses.firstOrNull()?.htmlReadyLicenseContent.orEmpty(),
                        ),
                    )
                }
            )
        }
    }
}
