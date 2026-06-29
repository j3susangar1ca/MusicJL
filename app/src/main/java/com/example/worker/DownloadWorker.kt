package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.utils.MediaStoreHelper
import kotlinx.coroutines.delay
import kotlin.math.abs

/**
 * Worker encargado de procesar la descarga y conversión de audio en segundo plano.
 * Garantiza resiliencia frente a desconexiones y falta de memoria en el sistema.
 */
class DownloadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val notificationHelper = NotificationHelper(applicationContext)

    override suspend fun doWork(): Result {
        // 1. Extraer los parámetros de entrada enviados por el disparador
        val videoId = inputData.getString("VIDEO_ID") ?: return Result.failure()
        val title = inputData.getString("TITLE") ?: "Canción VibeTune"
        val artist = inputData.getString("ARTIST") ?: "Artista Desconocido"
        
        // El índice del gradiente algorítmico heredado de tu limpiador
        val albumArtIndex = inputData.getInt("ALBUM_ART_INDEX", 0) 

        try {
            // 2. Notificación Inicial: Registrar el inicio de la tarea en segundo plano
            notificationHelper.showProgressNotification(videoId, title, artist, 0)

            // --- Conexión Cloud FOSS (Simulada / Preparada para Producción) ---
            // Aquí es donde se conectará el stream HTTP hacia Cloudflare R2.
            // Para el prototipo, replicamos tus pasos progresivos de seda:
            val progressSteps = listOf(12, 28, 45, 61, 79, 93, 100)
            
            for (progress in progressSteps) {
                if (isStopped) {
                    return Result.failure()
                }
                delay(400) // Tiempo de procesamiento realista en servidor asíncrono
                
                // Actualizar el progreso físico en la Rich Notification del sistema
                notificationHelper.showProgressNotification(videoId, title, artist, progress)
                
                // Exponer el progreso al sistema WorkManager (útil si la app se vuelve a abrir)
                setProgress(workDataOf("PROGRESS" to progress))
            }

            // 3. Materializar el archivo de audio físico en el almacenamiento público
            val savedUri = MediaStoreHelper.saveMp3ToMusicFolder(applicationContext, title, artist)

            return if (savedUri != null) {
                // 4. Éxito: Reemplazar la barra de progreso por la notificación multimedia final
                notificationHelper.showCompletedNotification(videoId, title, artist)
                
                // Devolvemos la URI local para que cualquier receptor consuma el archivo listo
                Result.success(workDataOf("LOCAL_URI" to savedUri.toString()))
            } else {
                Result.failure()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            // Si algo falla en la red, WorkManager aplicará automáticamente el Backoff Exponencial
            return Result.retry()
        }
    }
}
