package yokai.domain.source.browse.filter.models

import eu.kanade.tachiyomi.source.model.FilterList

data class SavedSearch(
    val id: Long,
    val name: String,
    val query: String,
    val filters: FilterList?,
)
