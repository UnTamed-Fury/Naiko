package naiko.domain.source.browse.filter.interactor

import naiko.domain.source.browse.filter.SavedSearchRepository

class DeleteSavedSearch(
    private val repository: SavedSearchRepository,
) {
    suspend fun await(searchId: Long) = repository.deleteById(searchId)
}
