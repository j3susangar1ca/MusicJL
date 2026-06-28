package com.example.domain.repository

import com.example.data.local.entity.DownloadEntity
import com.example.data.local.entity.DownloadStatus
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del Repositorio que define el contrato de acceso a datos 
 * para la gestión de descargas y metadatos musicales.
 */
interface DownloadRepository {

    /**
     * Obtiene el flujo reactivo de todas las descargas del historial.
     */
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    /**
     * Registra una nueva solicitud de descarga en el sistema.
     */
    suspend fun addDownloadToQueue(download: DownloadEntity)

    /**
     * Busca una descarga específica por su ID de YouTube.
     */
    suspend fun getDownloadById(videoId: String): DownloadEntity?

    /**
     * Actualiza el progreso numérico de la descarga física del MP3.
     */
    suspend fun updateProgress(videoId: String, progress: Int, status: DownloadStatus)

    /**
     * Guarda los metadatos definitivos obtenidos tras el consenso en la nube o IA.
     */
    suspend fun updateTrackMetadata(
        videoId: String,
        titleClean: String,
        artist: String,
        album: String?,
        coverArtUrl: String?,
        bpm: Int?,
        camelotKey: String?,
        status: DownloadStatus
    )

    /**
     * Marca la descarga como exitosa y asigna las rutas locales de almacenamiento.
     */
    suspend fun completeDownload(videoId: String, localUri: String, lyricsPath: String?)

    /**
     * Elimina una descarga del historial o de la cola activa.
     */
    suspend fun removeDownload(videoId: String)
}