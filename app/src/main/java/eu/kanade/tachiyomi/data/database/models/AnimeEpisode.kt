package eu.kanade.tachiyomi.data.database.models

import eu.kanade.tachiyomi.domain.anime.models.Anime
import eu.kanade.tachiyomi.animesource.model.AnimeUpdateStrategy

class AnimeEpisode(val anime: Anime, val episode: Episode) {
    companion object {
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
            episodeId: Long,
            _animeId: Long,
            episodeUrl: String,
            name: String,
            scanlator: String?,
            seen: Boolean,
            bookmark: Boolean,
            lastSecondSeen: Long,
            totalSeconds: Long,
            episodeNumber: Float,
            sourceOrder: Int,
            dateFetch: Long,
            dateUpload: Long,
        ) = AnimeEpisode(
            Anime.mapper(
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
                // filteredScanlators = filteredScanlators, // Might need to check if Anime has this
                updateStrategy = updateStrategy,
                coverLastModified = coverLastModified,
            ),
            Episode.mapper(
                id = episodeId,
                animeId = _animeId,
                url = episodeUrl,
                name = name,
                scanlator = scanlator,
                seen = seen,
                bookmark = bookmark,
                lastSecondSeen = lastSecondSeen,
                totalSeconds = totalSeconds,
                episodeNumber = episodeNumber,
                sourceOrder = sourceOrder,
                dateFetch = dateFetch,
                dateUpload = dateUpload,
            ),
        )
    }
}