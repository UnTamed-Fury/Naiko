package naiko.domain.extensionrepo.manga.interactor

import naiko.domain.extensionrepo.manga.repository.MangaExtensionRepoRepository

class DeleteMangaExtensionRepo(
    private val repository: MangaExtensionRepoRepository,
) {
    suspend fun await(baseUrl: String) {
        repository.deleteRepo(baseUrl)
    }
}
