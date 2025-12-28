package naiko.domain.extensionrepo.anime.interactor

import kotlinx.coroutines.flow.Flow
import naiko.domain.extensionrepo.anime.repository.AnimeExtensionRepoRepository
import naiko.domain.extensionrepo.model.ExtensionRepo

class GetAnimeExtensionRepo(
    private val repository: AnimeExtensionRepoRepository,
) {
    fun subscribeAll(): Flow<List<ExtensionRepo>> = repository.subscribeAll()

    suspend fun getAll(): List<ExtensionRepo> = repository.getAll()
}
