package eu.kanade.tachiyomi.data.track.myanimelist.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MALOAuth(
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @SerialName("expires_in")
    val expiresIn: Long,
) {
    // Assumes expired a minute earlier
    private val adjustedExpiresIn: Long = (expiresIn - 60) * 1000
    fun isExpired() = createdAt + adjustedExpiresIn < System.currentTimeMillis()
}
