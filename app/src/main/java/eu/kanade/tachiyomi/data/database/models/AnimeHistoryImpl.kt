package eu.kanade.tachiyomi.data.database.models

/**
 * Object containing the history statistics of an episode
 */
class AnimeHistoryImpl : AnimeHistory {

    /**
     * Id of history object.
     */
    override var id: Long? = null

    /**
     * Episode id of history object.
     */
    override var episode_id: Long = 0

    /**
     * Last time episode was seen in time long format
     */
    override var last_seen: Long = 0
}
