package naiko.domain.extensionrepo.manga.interactor

import naiko.domain.extensionrepo.manga.repository.MangaExtensionRepoRepository
import naiko.domain.extensionrepo.model.ExtensionRepo

class ReplaceMangaExtensionRepo(
    private val repository: MangaExtensionRepoRepository,
) {
    suspend fun await(repo: ExtensionRepo) {
        repository.replaceRepo(repo)
    }
}
