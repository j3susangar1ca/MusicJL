package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val youtubeUrl: String,
    val title: String,
    val artist: String,
    val status: String,
    val progress: Int,
    val bpm: Int?,
    val key: String? // Tono
)
