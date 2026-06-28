package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa los estados posibles en el ciclo de vida de una descarga en VibeTune.
 */
enum class DownloadStatus {
    QUEUED,       // En cola, esperando conexión o asignación del Worker
    PROCESSING,   // El backend en Hugging Face está extrayendo/limpiando con IA
    DOWNLOADING,  // Descargando el flujo de bytes del MP3/Opus final desde Cloudflare R2
    SUCCESS,      // Completado e inyectado en la MediaStore del dispositivo
    FAILED        // Error en el pipeline (se aplicará backoff exponencial)
}

/**
 * Entidad de Room que actúa como fuente única de verdad para la cola de descargas
 * e historial musical de la aplicación.
 */
@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey 
    val videoId: String,                 // ID único extraído de la URL de YouTube
    val url: String,                     // URL completa compartida por el usuario
    val titleRaw: String,                // Título original "sucio" del video de YouTube
    val titleClean: String? = null,      // Título limpio devuelto por la IA (Gemma/Llama)
    val artist: String? = null,          // Artista o banda principal identificado
    val album: String? = null,           // Álbum inferido mediante el motor de consenso
    val coverArtUrl: String? = null,     // URL de la carátula oficial en alta resolución (iTunes API)
    val status: DownloadStatus = DownloadStatus.QUEUED, // Estado actual del proceso
    val progress: Int = 0,               // Progreso real de la descarga física (0 a 100)
    
    // --- Atributos Avanzados de Grado Audiófilo ---
    val bpm: Int? = null,                // BPM calculados en la nube por Essentia/Librosa
    val camelotKey: String? = null,      // Clave tonal adaptada al sistema Camelot (ej: "4A")
    val lyricsPath: String? = null,      // Ruta local al archivo de letras sincronizadas (.lrc)
    val localUri: String? = null,        // Ruta de almacenamiento final del archivo en MediaStore
    val timestamp: Long = System.currentTimeMillis() // Fecha de agregado para ordenar la lista
)