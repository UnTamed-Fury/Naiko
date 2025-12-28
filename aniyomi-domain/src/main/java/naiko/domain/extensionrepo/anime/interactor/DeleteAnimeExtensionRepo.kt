package naiko.domain.extensionrepo.anime.interactor

import naiko.domain.extensionrepo.anime.repository.AnimeExtensionRepoRepository

class DeleteAnimeExtensionRepo(
    private val repository: AnimeExtensionRepoRepository,
) {
    suspend fun await(baseUrl: String) {
        repository.deleteRepo(baseUrl)
    }
}
