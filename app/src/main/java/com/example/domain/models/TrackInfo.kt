package com.example.domain.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Representa los metadatos puros de una canción devueltos por la API de conversión.
 */
@JsonClass(generateAdapter = true)
data class TrackInfo(
    @Json(name = "video_id") val videoId: String,
    val title: String,
    val artist: String,
    val album: String?,
    @Json(name = "cover_art_url") val coverArtUrl: String?,
    @Json(name = "download_url") val downloadUrl: String,
    val bpm: Int?,
    @Json(name = "camelot_key") val camelotKey: String?
)
