package com.example.data.repository

import com.example.data.local.DownloadDao
import com.example.data.local.entity.DownloadEntity
import com.example.data.local.entity.DownloadStatus
import com.example.domain.repository.DownloadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Implementación concreta del repositorio de descargas.
 * Centraliza el acceso a la base de datos local bajo el despachador de E/S (IO).
 */
class DownloadRepositoryImpl(
    private val downloadDao: DownloadDao
) : DownloadRepository {

    override fun getAllDownloads(): Flow<List<DownloadEntity>> {
        return downloadDao.getAllDownloadsFlow()
    }

    override suspend fun addDownloadToQueue(download: DownloadEntity) = withContext(Dispatchers.IO) {
        downloadDao.insertDownload(download)
    }

    override suspend fun getDownloadById(videoId: String): DownloadEntity? = withContext(Dispatchers.IO) {
        downloadDao.getDownloadById(videoId)
    }

    override suspend fun updateProgress(videoId: String, progress: Int, status: DownloadStatus) = withContext(Dispatchers.IO) {
        downloadDao.updateDownloadProgress(videoId, progress, status)
    }

    override suspend fun updateTrackMetadata(
        videoId: String,
        titleClean: String,
        artist: String,
        album: String?,
        coverArtUrl: String?,
        bpm: Int?,
        camelotKey: String?,
        status: DownloadStatus
    ) = withContext(Dispatchers.IO) {
        downloadDao.updateMetadata(
            videoId = videoId,
            titleClean = titleClean,
            artist = artist,
            album = album,
            coverArtUrl = coverArtUrl,
            bpm = bpm,
            camelotKey = camelotKey,
            status = status
        )
    }

    override suspend fun completeDownload(videoId: String, localUri: String, lyricsPath: String?) = withContext(Dispatchers.IO) {
        downloadDao.markAsSuccess(videoId, localUri, lyricsPath)
    }

    override suspend fun removeDownload(videoId: String) = withContext(Dispatchers.IO) {
        downloadDao.deleteDownloadById(videoId)
    }
}
