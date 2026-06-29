package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.example.data.remote.SupabaseClient
import com.example.utils.MediaStoreHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

/**
 * Worker encargado de procesar la descarga y conversión de audio en segundo plano.
 * Descarga el archivo desde el stream_url, normaliza el audio con FFmpeg y lo guarda en MediaStore.
 */
class DownloadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val notificationHelper = NotificationHelper(applicationContext)

    override suspend fun doWork(): Result {
        val videoId = inputData.getString("VIDEO_ID") ?: return Result.failure()
        val title = inputData.getString("TITLE") ?: "Canción VibeTune"
        val artist = inputData.getString("ARTIST") ?: "Artista Desconocido"
        val streamUrl = inputData.getString("STREAM_URL") ?: return Result.failure()

        try {
            notificationHelper.showProgressNotification(videoId, title, artist, 0)

            // 1. Descargar el archivo original a un temporal
            val tempFile = File(applicationContext.cacheDir, "temp_$videoId.mp3")
            downloadFile(streamUrl, tempFile)
            notificationHelper.showProgressNotification(videoId, title, artist, 50)

            // 2. Normalizar el audio con FFmpeg
            val normalizedFile = File(applicationContext.cacheDir, "normalized_$videoId.mp3")
            val ffmpegCommand = "-i \"${tempFile.absolutePath}\" -filter:a \"loudnorm\" \"${normalizedFile.absolutePath}\" -y"
            
            val session = FFmpegKit.execute(ffmpegCommand)
            if (ReturnCode.isSuccess(session.returnCode)) {
                notificationHelper.showProgressNotification(videoId, title, artist, 90)

                // 3. Guardar el archivo normalizado en el almacenamiento público
                val savedUri = MediaStoreHelper.saveMp3ToMusicFolder(applicationContext, title, artist, normalizedFile)

                // Limpieza de archivos temporales
                tempFile.delete()
                normalizedFile.delete()

                return if (savedUri != null) {
                    notificationHelper.showCompletedNotification(videoId, title, artist)
                    Result.success(workDataOf("LOCAL_URI" to savedUri.toString()))
                } else {
                    Result.failure()
                }
            } else {
                tempFile.delete()
                normalizedFile.delete()
                return Result.failure()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }

    private suspend fun downloadFile(url: String, targetFile: File) = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).build()
        // Reutilizamos el okHttpClient de SupabaseClient
        // Pero SupabaseClient.okHttpClient es privado.
        // Vamos a usar una instancia nueva o hacerlo público.
        // Mirando SupabaseClient.kt, okHttpClient es privado.
        
        val client = okhttp3.OkHttpClient() 
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Error al descargar: $response")
            response.body?.byteStream()?.use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
    }
}
