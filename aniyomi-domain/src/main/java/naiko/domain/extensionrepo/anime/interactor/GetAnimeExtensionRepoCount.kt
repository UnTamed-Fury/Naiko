package naiko.domain.extensionrepo.anime.interactor

import naiko.domain.extensionrepo.anime.repository.AnimeExtensionRepoRepository

class GetAnimeExtensionRepoCount(
    private val repository: AnimeExtensionRepoRepository,
) {
    fun subscribe() = repository.getCount()
}
