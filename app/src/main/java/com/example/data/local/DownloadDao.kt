package com.example.data.local

import androidx.room.*
import com.example.data.local.entity.DownloadEntity
import com.example.data.local.entity.DownloadStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    // Registra una nueva descarga en la cola (por defecto en estado QUEUED)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)

    // Observa toda la lista de descargas e historial ordenada por fecha (Reactiva para la UI)
    @Query("SELECT * FROM downloads ORDER BY timestamp DESC")
    fun getAllDownloadsFlow(): Flow<List<DownloadEntity>>

    // Consulta una canción específica de forma directa (útil para el Worker)
    @Query("SELECT * FROM downloads WHERE videoId = :videoId LIMIT 1")
    suspend fun getDownloadById(videoId: String): DownloadEntity?

    // Actualiza únicamente el progreso en bytes y el estado (Evita reescribir toda la fila de golpe)
    @Query("UPDATE downloads SET progress = :progress, status = :status WHERE videoId = :videoId")
    suspend fun updateDownloadProgress(videoId: String, progress: Int, status: DownloadStatus)

    // Inyecta los metadatos e información audiófila limpia una vez procesada en la nube
    @Query("""
        UPDATE downloads SET 
            titleClean = :titleClean, 
            artist = :artist, 
            album = :album, 
            coverArtUrl = :coverArtUrl, 
            bpm = :bpm, 
            camelotKey = :camelotKey, 
            status = :status 
        WHERE videoId = :videoId
    """)
    suspend fun updateMetadata(
        videoId: String,
        titleClean: String,
        artist: String,
        album: String?,
        coverArtUrl: String?,
        bpm: Int?,
        camelotKey: String?,
        status: DownloadStatus
    )

    // Registra la ruta final del archivo guardado en el teléfono al completarse
    @Query("UPDATE downloads SET localUri = :localUri, lyricsPath = :lyricsPath, status = 'SUCCESS' WHERE videoId = :videoId")
    suspend fun markAsSuccess(videoId: String, localUri: String, lyricsPath: String?)

    // Elimina un registro de la cola o historial local
    @Query("DELETE FROM downloads WHERE videoId = :videoId")
    suspend fun deleteDownloadById(videoId: String)
}