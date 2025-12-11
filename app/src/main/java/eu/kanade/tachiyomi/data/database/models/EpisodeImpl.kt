package eu.kanade.tachiyomi.data.database.models

import eu.kanade.tachiyomi.animesource.model.SEpisode

fun SEpisode.toEpisode(): EpisodeImpl {
    return EpisodeImpl().apply {
        name = this@toEpisode.name
        url = this@toEpisode.url
        date_upload = this@toEpisode.date_upload
        episode_number = this@toEpisode.episode_number
        scanlator = this@toEpisode.scanlator
    }
}


class EpisodeImpl : Episode {

    override var id: Long? = null

    override var anime_id: Long? = null

    override lateinit var url: String

    override lateinit var name: String

    override var scanlator: String? = null

    override var seen: Boolean = false

    override var bookmark: Boolean = false

    override var last_second_seen: Long = 0

    override var total_seconds: Long = 0

    override var date_fetch: Long = 0

    override var date_upload: Long = 0

    override var episode_number: Float = 0f

    override var source_order: Int = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val episode = other as Episode
        return url == episode.url
    }

    override fun hashCode(): Int {
        return url.hashCode()
    }
}