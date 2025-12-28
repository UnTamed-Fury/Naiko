package naiko.domain.extensionrepo.manga.interactor

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import naiko.domain.extensionrepo.manga.repository.MangaExtensionRepoRepository
import naiko.domain.extensionrepo.model.ExtensionRepo
import naiko.domain.extensionrepo.service.ExtensionRepoService

class UpdateMangaExtensionRepo(
    private val repository: MangaExtensionRepoRepository,
    private val service: ExtensionRepoService,
) {

    suspend fun awaitAll() = coroutineScope {
        repository.getAll()
            .map { async { await(it) } }
            .awaitAll()
    }

    suspend fun await(repo: ExtensionRepo) {
        val newRepo = service.fetchRepoDetails(repo.baseUrl) ?: return
        if (
            repo.signingKeyFingerprint.startsWith("NOFINGERPRINT") ||
            repo.signingKeyFingerprint == newRepo.signingKeyFingerprint
        ) {
            repository.upsertRepo(newRepo)
        }
    }
}
