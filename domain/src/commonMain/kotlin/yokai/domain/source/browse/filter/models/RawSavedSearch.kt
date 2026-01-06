package yokai.domain.source.browse.filter.models

data class RawSavedSearch(
    val id: Long,
    val sourceId: Long,
    val name: String,
    val query: String?,
    val filtersJson: String?,
) {
    companion object {
        fun mapper(
            id: Long,
            sourceId: Long,
            name: String,
            query: String?,
            filtersJson: String?,
        ) = RawSavedSearch(
            id = id,
            sourceId = sourceId,
            name = name,
            query = query,
            filtersJson = filtersJson,
        )
    }
}
