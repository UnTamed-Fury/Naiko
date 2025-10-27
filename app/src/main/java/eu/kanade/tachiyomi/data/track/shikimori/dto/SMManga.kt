package eu.kanade.tachiyomi.data.track.shikimori.dto

import eu.kanade.tachiyomi.data.track.model.TrackSearch
import eu.kanade.tachiyomi.data.track.shikimori.ShikimoriApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SMManga(
    val id: Long,
    val name: String,
    val chapters: Long,
    val image: SMMangaCover,
    val score: Double,
    val url: String,
    val status: String,
    val kind: String,
    @SerialName("aired_on")
    val airedOn: String?,
) {
    fun toTrack(trackId: Long): TrackSearch {
        return TrackSearch.create(trackId).apply {
            media_id = this@SMManga.id
            title = name
            total_chapters = chapters
            cover_url = ShikimoriApi.BASE_URL + image.preview
            summary = ""
            score = this@SMManga.score.toFloat()
            tracking_url = ShikimoriApi.BASE_URL + url
            publishing_status = this@SMManga.status
            publishing_type = kind
            start_date = airedOn ?: ""
        }
    }
}

@Serializable
data class SMMangaCover(
    val preview: String,
)
