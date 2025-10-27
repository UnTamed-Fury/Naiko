package eu.kanade.tachiyomi.ui.source.browse

import eu.kanade.tachiyomi.source.model.FilterList
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import yokai.domain.source.browse.filter.FilterSerializer
import yokai.domain.source.browse.filter.models.RawSavedSearch
import yokai.domain.source.browse.filter.models.SavedSearch

fun RawSavedSearch.applySave(
    originalFilters: FilterList,
    json: Json = Injekt.get(),
    filterSerializer: FilterSerializer = Injekt.get(),
): SavedSearch {
    val rt = SavedSearch(
        id = this.id,
        name = this.name,
        query = this.query.orEmpty(),
        filters = null,
    )
    if (filtersJson == null) {
        return rt
    }

    val filters = try {
        json.decodeFromString<JsonArray>(filtersJson!!)
    } catch (e: Exception) {
        null
    } ?: return rt

    try {
        filterSerializer.deserialize(originalFilters, filters)
        return rt.copy(filters = originalFilters)
    } catch (e: Exception) {
        return rt
    }
}

fun List<RawSavedSearch>.applyAllSave(
    originalFilters: FilterList,
    json: Json = Injekt.get(),
    filterSerializer: FilterSerializer = Injekt.get(),
) = this.map { it.applySave(originalFilters, json, filterSerializer) }
