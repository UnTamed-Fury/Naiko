package naiko.domain.extensionrepo.manga.interactor

import kotlinx.coroutines.flow.Flow
import naiko.domain.extensionrepo.manga.repository.MangaExtensionRepoRepository
import naiko.domain.extensionrepo.model.ExtensionRepo

class GetMangaExtensionRepo(
    private val repository: MangaExtensionRepoRepository,
) {
    fun subscribeAll(): Flow<List<ExtensionRepo>> = repository.subscribeAll()

    suspend fun getAll(): List<ExtensionRepo> = repository.getAll()
}
