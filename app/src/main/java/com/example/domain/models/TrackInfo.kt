package com.example.domain.models

data class TrackInfo(
    val id: String,
    val title: String,
    val artist: String,
    val youtubeUrl: String,
    val durationSeconds: Int,
    val bpm: Int?,
    val key: String? // Tono
)
