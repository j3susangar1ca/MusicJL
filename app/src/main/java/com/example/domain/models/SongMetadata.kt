package com.example.domain.models

// Cleaned metadata parsed from raw YouTube title
data class SongMetadata(
    val title: String,
    val artist: String,
    val albumArtIndex: Int
)
