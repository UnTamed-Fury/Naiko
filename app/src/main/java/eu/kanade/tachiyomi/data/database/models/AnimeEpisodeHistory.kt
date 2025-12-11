package eu.kanade.tachiyomi.data.database.models

import eu.kanade.tachiyomi.domain.anime.models.Anime
import eu.kanade.tachiyomi.animesource.model.AnimeUpdateStrategy

/**
 * Object containing anime, episode and history
 *
 * @param anime object containing anime
 * @param episode object containing episode
 * @param history object containing history
 */
data class AnimeEpisodeHistory(val anime: Anime, val episode: Episode, val history: AnimeHistory, var extraEpisodes: List<EpisodeHistory> = emptyList()) {

    companion object {
        fun createBlank() = AnimeEpisodeHistory(
            AnimeImpl(null, -1, ""),
            EpisodeImpl(),
            AnimeHistoryImpl(),
        )

        fun mapper(
            // anime
            animeId: Long,
            source: Long,
            animeUrl: String,
            artist: String?,
            author: String?,
            description: String?,
            genre: String?,
            title: String,
            status: Long,
            thumbnailUrl: String?,
            favorite: Boolean,
            lastUpdate: Long?,
            initialized: Boolean,
            viewer: Long,
            hideTitle: Boolean,
            episodeFlags: Long,
            dateAdded: Long?,
            filteredScanlators: String?,
            updateStrategy: AnimeUpdateStrategy,
            coverLastModified: Long,
            // episode
            episodeId: Long?,
            episodeAnimeId: Long?,
            episodeUrl: String?,
            name: String?,
            scanlator: String?,
            seen: Boolean?,
            bookmark: Boolean?,
            lastSecondSeen: Long?,
            totalSeconds: Long?,
            episodeNumber: Float?,
            sourceOrder: Int?,
            dateFetch: Long?,
            dateUpload: Long?,
            // history
            historyId: Long?,
            historyEpisodeId: Long?,
            historyLastSeen: Long?,
        ): AnimeEpisodeHistory {
            val anime = Anime.mapper(
                id = animeId,
                source = source,
                url = animeUrl,
                artist = artist,
                author = author,
                description = description,
                genre = genre,
                title = title,
                status = status,
                thumbnailUrl = thumbnailUrl,
                favorite = favorite,
                lastUpdate = lastUpdate,
                initialized = initialized,
                viewerFlags = viewer,
                hideTitle = hideTitle,
                episodeFlags = episodeFlags,
                dateAdded = dateAdded,
                // filteredScanlators = filteredScanlators,
                updateStrategy = updateStrategy,
                coverLastModified = coverLastModified,
            )

            val episode = try {
                Episode.mapper(
                    id = episodeId!!,
                    animeId = episodeAnimeId!!,
                    url = episodeUrl!!,
                    name = name!!,
                    scanlator = scanlator,
                    seen = seen!!,
                    bookmark = bookmark!!,
                    lastSecondSeen = lastSecondSeen!!,
                    totalSeconds = totalSeconds!!,
                    episodeNumber = episodeNumber!!,
                    sourceOrder = sourceOrder!!,
                    dateFetch = dateFetch!!,
                    dateUpload = dateUpload!!,
                )
            } catch (_: NullPointerException) {
                EpisodeImpl()
            }

            val history = try {
                AnimeHistory.mapper(
                    id = historyId!!,
                    episodeId = historyEpisodeId!!,
                    lastSeen = historyLastSeen,
                )
            } catch (_: NullPointerException) {
                AnimeHistoryImpl().apply {
                    historyEpisodeId?.let { episode_id = it }
                    historyLastSeen?.let { last_seen = it }
                }
            }

            return AnimeEpisodeHistory(anime, episode, history)
        }
    }
}

data class EpisodeHistory(val episode: Episode, var history: AnimeHistory? = null) : Episode by episode