package com.example.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.domain.models.DownloadedSong
import java.io.File

/**
 * Utilidad de bajo nivel encargada de interactuar con la biblioteca de medios 
 * de Android (MediaStore) bajo las reglas de Scoped Storage.
 */
object MediaStoreHelper {

    private const val RELATIVE_PATH_VIBETUNE = "${Environment.DIRECTORY_MUSIC}/VibeTune"

    /**
     * Guarda un archivo MP3 simulado o real en la carpeta pública de Música del dispositivo.
     * Requerido por VibeTuneViewModel para finalizar el flujo de descarga.
     */
    fun saveMp3ToMusicFolder(context: Context, title: String, artist: String): Uri? {
        val resolver = context.contentResolver
        
        // Configurar los metadatos de indexación para el sistema de Android
        val audioDetails = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "$artist - $title.mp3")
            put(MediaStore.Audio.Media.TITLE, title)
            put(MediaStore.Audio.Media.ARTIST, artist)
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
            
            // Reglas de ruta física basadas en la API de destino (Scoped Storage)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Audio.Media.RELATIVE_PATH, RELATIVE_PATH_VIBETUNE)
                put(MediaStore.Audio.Media.IS_PENDING, 1) // Bloquea el archivo mientras se escribe
            }
        }

        val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val fileUri = resolver.insert(collectionUri, audioDetails) ?: return null

        try {
            // Escribir un flujo de bytes simulado (Dummy MP3 válido) para el prototipo
            resolver.openOutputStream(fileUri).use { outputStream ->
                if (outputStream != null) {
                    // Escribimos una cabecera dummy minimalista para que los reproductores lo reconozcan
                    val dummyMp3Header = byteArrayOf(0x49, 0x44, 0x33, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
                    outputStream.write(dummyMp3Header)
                    // Rellenar con bytes vacíos para simular peso de archivo (aprox 3MB)
                    outputStream.write(ByteArray(1024 * 1024 * 3)) 
                    outputStream.flush()
                }
            }

            // Liberar el estado pendiente para que aparezca en los reproductores globales
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                audioDetails.clear()
                audioDetails.put(MediaStore.Audio.Media.IS_PENDING, 0)
                resolver.update(fileUri, audioDetails, null, null)
            }
            
            return fileUri
        } catch (e: Exception) {
            e.printStackTrace()
            // En caso de fallo, limpiamos el registro huérfano de la base de datos
            resolver.delete(fileUri, null, null)
            return null
        }
    }

    /**
     * Consulta el almacenamiento externo para listar todas las canciones guardadas 
     * específicamente por la aplicación VibeTune.
     */
    fun getDownloadedVibeTuneSongs(context: Context): List<DownloadedSong> {
        val songsList = mutableListOf<DownloadedSong>()
        val resolver = context.contentResolver

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST
        )

        // Filtramos para traer únicamente los archivos dentro de nuestra carpeta contenedora
        val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "${MediaStore.Audio.Media.RELATIVE_PATH} LIKE ?"
        } else {
            "${MediaStore.Audio.Media.DATA} LIKE ?"
        }
        
        val selectionArgs = arrayOf("%$RELATIVE_PATH_VIBETUNE%")

        val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        resolver.query(
            collectionUri,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val contentUri = Uri.withAppendedPath(collectionUri, id.toString())

                // Mapeo directo hacia tu modelo de dominio DownloadedSong
                songsList.add(
                    DownloadedSong(
                        id = id.toString(),
                        title = title ?: "Canción Desconocida",
                        artist = artist ?: "Artista Desconocido",
                        localUri = contentUri
                    )
                )
            }
        }

        return songsList
    }
}