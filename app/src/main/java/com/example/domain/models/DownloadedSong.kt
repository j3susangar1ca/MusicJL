package com.example.domain.models

import android.net.Uri

// Data model representing a downloaded MP3 track
data class DownloadedSong(
    val id: Long,
    val title: String,
    val artist: String,
    val uri: Uri
)
