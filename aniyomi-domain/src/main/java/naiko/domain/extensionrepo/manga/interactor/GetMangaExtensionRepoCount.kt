package naiko.domain.extensionrepo.manga.interactor

import naiko.domain.extensionrepo.manga.repository.MangaExtensionRepoRepository

class GetMangaExtensionRepoCount(
    private val repository: MangaExtensionRepoRepository,
) {
    fun subscribe() = repository.getCount()
}
