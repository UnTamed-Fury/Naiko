package eu.kanade.tachiyomi.data.database.models

import java.io.Serializable

/**
 * Object containing the history statistics of an episode
 */
interface AnimeHistory : Serializable {

    /**
     * Id of history object.
     */
    var id: Long?

    /**
     * Episode id of history object.
     */
    var episode_id: Long

    /**
     * Last time episode was seen in time long format
     */
    var last_seen: Long

    companion object {

        /**
         * History constructor
         *
         * @param episode episode object
         * @return history object
         */
        fun create(episode: Episode): AnimeHistory = AnimeHistoryImpl().apply {
            this.episode_id = episode.id!!
        }

        fun create(): AnimeHistory = AnimeHistoryImpl()

        fun mapper(
            id: Long,
            episodeId: Long,
            lastSeen: Long?,
        ): AnimeHistory = AnimeHistoryImpl().apply {
            this.id = id
            this.episode_id = episodeId
            lastSeen?.let { this.last_seen = it }
        }
    }
}
