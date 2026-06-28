package com.example.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.DownloadedSong

/**
 * MediaStoreHelper saves dummy MP3 downloads into the device's shared Music/VibeTune folder
 * and queries existing VibeTune downloads.
 */
object MediaStoreHelper {

    /**
     * Saves a high-fidelity mock MP3 track to the Android MediaStore under the standard Music directory.
     */
    fun saveMp3ToMusicFolder(context: Context, title: String, artist: String): Uri? {
        val resolver = context.contentResolver
        val audioCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        
        val safeTitle = title.replace("/", "_").replace("\\", "_")
        val displayName = "MusicJL_${System.currentTimeMillis()}_${safeTitle}.mp3"
        
        val contentValues = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Audio.Media.TITLE, title)
            put(MediaStore.Audio.Media.ARTIST, artist)
            put(MediaStore.Audio.Media.ALBUM, "Music JL Downloads")
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Audio.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MUSIC}/MusicJL")
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            }
        }
        
        val itemUri = resolver.insert(audioCollection, contentValues)
        
        if (itemUri != null) {
            try {
                resolver.openOutputStream(itemUri).use { outputStream ->
                    if (outputStream != null) {
                        // Write 100KB of simulated premium MP3 audio frames (dummy content)
                        val dummyBuffer = ByteArray(1024 * 100)
                        outputStream.write(dummyBuffer)
                    }
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Audio.Media.IS_PENDING, 0)
                    resolver.update(itemUri, contentValues, null, null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
        return itemUri
    }

    /**
     * Queries the MediaStore database to retrieve previously saved VibeTune downloads,
     * populating the dashboard history in real-time.
     */
    fun getDownloadedVibeTuneSongs(context: Context): List<DownloadedSong> {
        val songList = mutableListOf<DownloadedSong>()
        val resolver = context.contentResolver
        val audioCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST
        )
        
        // Filter by tracks belonging to "Music JL Downloads" album
        val selection = "${MediaStore.Audio.Media.ALBUM} = ?"
        val selectionArgs = arrayOf("Music JL Downloads")
        
        // Sort descending by ID to show newest downloads first
        val sortOrder = "${MediaStore.Audio.Media._ID} DESC"
        
        try {
            resolver.query(audioCollection, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol) ?: "Unknown Song"
                    val artist = cursor.getString(artistCol) ?: "Unknown Artist"
                    val contentUri = Uri.withAppendedPath(audioCollection, id.toString())
                    
                    songList.add(DownloadedSong(id, title, artist, contentUri))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return songList
    }
}
