package yokai.domain.source.browse.filter.interactor

import yokai.domain.source.browse.filter.SavedSearchRepository

class DeleteSavedSearch(
    private val repository: SavedSearchRepository,
) {
    suspend fun await(searchId: Long) = repository.deleteById(searchId)
}
