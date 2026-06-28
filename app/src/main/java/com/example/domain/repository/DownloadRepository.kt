package com.example.domain.repository

import com.example.domain.models.TrackInfo
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    fun getAllTracks(): Flow<List<TrackInfo>>
    suspend fun addTrackToDownloadQueue(track: TrackInfo)
    suspend fun updateTrackDownloadStatus(trackId: String, status: String, progress: Int)
}
