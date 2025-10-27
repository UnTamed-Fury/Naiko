package eu.kanade.tachiyomi.data.track.mangaupdates

import eu.kanade.tachiyomi.data.database.models.Track
import eu.kanade.tachiyomi.data.track.mangaupdates.dto.MUContext
import eu.kanade.tachiyomi.data.track.mangaupdates.dto.MUListItem
import eu.kanade.tachiyomi.data.track.mangaupdates.dto.MULoginResponse
import eu.kanade.tachiyomi.data.track.mangaupdates.dto.MURating
import eu.kanade.tachiyomi.data.track.mangaupdates.dto.MURecord
import eu.kanade.tachiyomi.data.track.mangaupdates.dto.MUSearchResult
import eu.kanade.tachiyomi.network.DELETE
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.network.PUT
import eu.kanade.tachiyomi.network.awaitSuccess
import eu.kanade.tachiyomi.network.parseAs
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import uy.kohesive.injekt.injectLazy

class MangaUpdatesApi(
    interceptor: MangaUpdatesInterceptor,
    private val client: OkHttpClient,
) {
    private val json: Json by injectLazy()

    private val authClient = client.newBuilder()
        .addInterceptor(interceptor)
        .build()

    suspend fun getSeriesListItem(track: Track): Pair<MUListItem, MURating?> {
        val listItem =
            authClient.newCall(
                GET(
                    url = "$BASE_URL/v1/lists/series/${track.media_id}",
                ),
            )
                .awaitSuccess()
                .parseAs<MUListItem>()

        val rating = getSeriesRating(track)

        return listItem to rating
    }

    suspend fun addSeriesToList(track: Track) {
        val body = createTrackBody(track)
        authClient.newCall(
            POST(
                url = "$BASE_URL/v1/lists/series",
                body = body.toString().toRequestBody(CONTENT_TYPE),
            ),
        )
            .awaitSuccess()
    }

    suspend fun removeSeriesFromList(track: Track): Boolean {
        val body = buildJsonArray {
            add(track.media_id)
        }
        authClient.newCall(
            POST(
                url = "$BASE_URL/v1/lists/series/delete",
                body = body.toString().toRequestBody(CONTENT_TYPE),
            ),
        )
            .awaitSuccess()
            .let { return it.code == 200 }
    }

    suspend fun updateSeriesListItem(track: Track) {
        val body = createTrackBody(track)
        authClient.newCall(
            POST(
                url = "$BASE_URL/v1/lists/series/update",
                body = body.toString().toRequestBody(CONTENT_TYPE),
            ),
        )
            .awaitSuccess()

        updateSeriesRating(track)
    }

    private fun createTrackBody(track: Track) = buildJsonArray {
        addJsonObject {
            putJsonObject("series") {
                put("id", track.media_id)
            }
            put("list_id", track.status)
            putJsonObject("status") {
                put("chapter", track.last_chapter_read.toInt())
            }
        }
    }

    private suspend fun getSeriesRating(track: Track): MURating? {
        return try {
            authClient.newCall(
                GET(
                    url = "$BASE_URL/v1/series/${track.media_id}/rating",
                ),
            )
                .awaitSuccess()
                .parseAs<MURating>()
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun updateSeriesRating(track: Track) {
        if (track.score != 0f) {
            val body = buildJsonObject {
                put("rating", track.score)
            }
            authClient.newCall(
                PUT(
                    url = "$BASE_URL/v1/series/${track.media_id}/rating",
                    body = body.toString().toRequestBody(CONTENT_TYPE),
                ),
            )
                .awaitSuccess()
        } else {
            authClient.newCall(
                DELETE(
                    url = "$BASE_URL/v1/series/${track.media_id}/rating",
                ),
            )
                .awaitSuccess()
        }
    }

    suspend fun search(query: String): List<MURecord> {
        val body = buildJsonObject {
            put("search", query)
            put(
                "filter_types",
                buildJsonArray {
                    add("drama cd")
                    add("novel")
                },
            )
        }
        return client.newCall(
            POST(
                url = "$BASE_URL/v1/series/search",
                body = body.toString().toRequestBody(CONTENT_TYPE),
            ),
        )
            .awaitSuccess()
            .parseAs<MUSearchResult>()
            .results
            .map { it.record }
    }

    suspend fun authenticate(username: String, password: String): MUContext? {
        val body = buildJsonObject {
            put("username", username)
            put("password", password)
        }
        return client.newCall(
            PUT(
                url = "$BASE_URL/v1/account/login",
                body = body.toString().toRequestBody(CONTENT_TYPE),
            ),
        )
            .awaitSuccess()
            .parseAs<MULoginResponse>()
            .context
    }

    companion object {
        private const val BASE_URL = "https://api.mangaupdates.com"
        private val CONTENT_TYPE = "application/vnd.api+json".toMediaType()
    }
}
