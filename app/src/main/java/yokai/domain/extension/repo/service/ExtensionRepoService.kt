package yokai.domain.extension.repo.service

import androidx.core.net.toUri
import co.touchlab.kermit.Logger
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.awaitSuccess
import eu.kanade.tachiyomi.network.parseAs
import eu.kanade.tachiyomi.util.system.withIOContext
import okhttp3.OkHttpClient
import yokai.domain.extension.repo.model.ExtensionRepo

class ExtensionRepoService(
    private val client: OkHttpClient,
) {

    suspend fun fetchRepoDetails(
        repo: String,
    ): ExtensionRepo? {
        return withIOContext {
            val url = "$repo/repo.json".toUri()

            try {
                client.newCall(GET(url.toString()))
                    .awaitSuccess()
                    .parseAs<ExtensionRepoMetaDto>()
                    .toExtensionRepo(baseUrl = repo)
            } catch (e: Exception) {
                Logger.e(e) { "Failed to fetch repo details" }
                null
            }
        }
    }
}
