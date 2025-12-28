package naiko.domain.source.browse.filter.interactor

import naiko.domain.source.browse.filter.SavedSearchRepository

class InsertSavedSearch(
    private val repository: SavedSearchRepository,
) {
    suspend fun await(sourceId: Long, name: String, query: String?, filtersJson: String?) = repository.insert(sourceId, name, query, filtersJson)
}
