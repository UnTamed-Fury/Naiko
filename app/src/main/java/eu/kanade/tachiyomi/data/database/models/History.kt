package eu.kanade.tachiyomi.data.database.models

import java.io.Serializable

/**
 * Object containing the history statistics of a chapter
 */
interface History : Serializable {

    /**
     * Id of history object.
     */
    var id: Long?

    /**
     * Chapter id of history object.
     */
    var chapter_id: Long

    /**
     * Last time chapter was read in time long format
     */
    var last_read: Long

    /**
     * Total time chapter was read
     */
    var time_read: Long

    companion object {

        /**
         * History constructor
         *
         * @param chapter chapter object
         * @return history object
         */
        fun create(chapter: Chapter): History = HistoryImpl().apply {
            this.chapter_id = chapter.id!!
        }

        fun create(): History = HistoryImpl()

        fun mapper(
            id: Long,
            chapterId: Long,
            lastRead: Long?,
            timeRead: Long?,
        ): History = HistoryImpl().apply {
            this.id = id
            this.chapter_id = chapterId
            lastRead?.let { this.last_read = it }
            timeRead?.let { this.time_read = it }
        }
    }
}
