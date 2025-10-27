package eu.kanade.tachiyomi.data.track.kitsu.dto

import eu.kanade.tachiyomi.data.track.TrackManager
import eu.kanade.tachiyomi.data.track.kitsu.KitsuApi
import eu.kanade.tachiyomi.data.track.model.TrackSearch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.serialization.Serializable

@Serializable
data class KitsuSearchResult(
    val media: KitsuSearchResultData,
)

@Serializable
data class KitsuSearchResultData(
    val key: String,
)

@Serializable
data class KitsuAlgoliaSearchResult(
    val hits: List<KitsuAlgoliaSearchItem>,
)

@Serializable
data class KitsuAlgoliaSearchItem(
    val id: Long,
    val canonicalTitle: String,
    val chapterCount: Long?,
    val subtype: String?,
    val posterImage: KitsuSearchItemCover?,
    val synopsis: String?,
    val averageRating: Double?,
    val startDate: Long?,
    val endDate: Long?,
) {
    fun toTrack(): TrackSearch {
        return TrackSearch.create(TrackManager.KITSU).apply {
            media_id = this@KitsuAlgoliaSearchItem.id
            title = canonicalTitle
            total_chapters = chapterCount ?: 0
            cover_url = posterImage?.original ?: ""
            summary = synopsis ?: ""
            tracking_url = KitsuApi.mangaUrl(media_id)
            score = averageRating?.toFloat() ?: -1.0F
            publishing_status = if (endDate == null) "Publishing" else "Finished"
            publishing_type = subtype ?: ""
            start_date = startDate?.let {
                val outputDf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                outputDf.format(Date(it * 1000))
            } ?: ""
        }
    }
}
