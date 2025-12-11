package yokai.domain.source.browse.filter

import kotlinx.coroutines.flow.Flow
import yokai.domain.source.browse.filter.models.RawSavedSearch

interface SavedSearchRepository {
    suspend fun findAll(): List<RawSavedSearch>
    fun subscribeAllBySourceId(sourceId: Long): Flow<List<RawSavedSearch>>
    suspend fun findAllBySourceId(sourceId: Long): List<RawSavedSearch>
    suspend fun findOneBySourceIdAndName(sourceId: Long, name: String): RawSavedSearch?
    suspend fun findById(id: Long): RawSavedSearch?
    suspend fun deleteById(id: Long)
    suspend fun insert(sourceId: Long, name: String, query: String?, filtersJson: String?): Long?
}
