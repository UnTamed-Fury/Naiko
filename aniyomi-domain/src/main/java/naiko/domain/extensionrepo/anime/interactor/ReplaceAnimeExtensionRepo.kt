package naiko.domain.extensionrepo.anime.interactor

import naiko.domain.extensionrepo.anime.repository.AnimeExtensionRepoRepository
import naiko.domain.extensionrepo.model.ExtensionRepo

class ReplaceAnimeExtensionRepo(
    private val repository: AnimeExtensionRepoRepository,
) {
    suspend fun await(repo: ExtensionRepo) {
        repository.replaceRepo(repo)
    }
}
