package yokai.domain.source.browse.filter.interactor

import yokai.domain.source.browse.filter.SavedSearchRepository

class GetSavedSearch(
    private val repository: SavedSearchRepository,
) {
    suspend fun awaitAll() = repository.findAll()
    suspend fun awaitAllBySourceId(sourceId: Long) = repository.findAllBySourceId(sourceId)
    fun subscribeAllBySourceId(sourceId: Long) = repository.subscribeAllBySourceId(sourceId)
    suspend fun awaitBySourceIdAndName(sourceId: Long, name: String) = repository.findOneBySourceIdAndName(sourceId, name)
    suspend fun awaitById(id: Long) = repository.findById(id)
}
