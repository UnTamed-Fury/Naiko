package yokai.presentation.extension.repo

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.CrossfadeTransition
import eu.kanade.tachiyomi.ui.base.controller.BaseComposeController

class ExtensionRepoController(private val repoUrl: String? = null) : BaseComposeController() {

    @Composable
    override fun ScreenContent() {
        Navigator(
            screen = ExtensionRepoScreen(
                title = "Extension Repos",
                repoUrl = repoUrl,
            ),
            content = {
                CrossfadeTransition(navigator = it)
            },
        )
    }
}
