package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.data.remote.SupabaseClient
import com.example.domain.models.DownloadedSong
import com.example.ui.screens.VibeUiState
import com.example.utils.IntentParser
import com.example.utils.MediaStoreHelper
import com.example.utils.MetadataCleaner
import com.example.worker.DownloadWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VibeTuneViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<VibeUiState>(VibeUiState.Idle)
    val uiState: StateFlow<VibeUiState> = _uiState.asStateFlow()

    private val _pastedUrl = MutableStateFlow("")
    val pastedUrl: StateFlow<String> = _pastedUrl.asStateFlow()

    private val _historyList = MutableStateFlow<List<DownloadedSong>>(emptyList())
    val historyList: StateFlow<List<DownloadedSong>> = _historyList.asStateFlow()

    fun updatePastedUrl(url: String) {
        _pastedUrl.value = url
    }

    fun refreshHistory(context: Context) {
        viewModelScope.launch {
            val songs = MediaStoreHelper.getDownloadedVibeTuneSongs(context)
            _historyList.value = songs
        }
    }

    /**
     * Intercepta el enlace, consulta Supabase en tiempo real y parsea la respuesta.
     */
    fun processUrl(context: Context, url: String) {
        val trimmedUrl = url.trim()
        if (trimmedUrl.isEmpty()) return

        val videoId = IntentParser.extractVideoId(trimmedUrl)
        if (videoId.isNullOrEmpty()) {
            _uiState.value = VibeUiState.Error("La URL proporcionada no es un enlace válido de YouTube.")
            return
        }

        viewModelScope.launch {
            _uiState.value = VibeUiState.Capturing
            try {
                // Consultar Supabase con el filtro correcto eq.
                val responseList = SupabaseClient.apiService.getConvertedTrackInfo(videoId = "eq.$videoId")
                
                if (responseList.isNotEmpty()) {
                    val track = responseList.first()
                    val cleaned = MetadataCleaner.cleanMetadata(track.title)

                    _uiState.value = VibeUiState.MetadataReady(
                        url = trimmedUrl,
                        rawTitle = track.title,
                        title = cleaned.title,
                        artist = track.artist.ifBlank { cleaned.artist },
                        albumArtIndex = cleaned.albumArtIndex,
                        streamUrl = track.downloadUrl
                    )
                } else {
                    // FALLBACK: El video no está en la base de datos de Supabase aún.
                    // Usamos el limpiador local para generar metadatos a partir del ID o una cadena genérica.
                    val fallbackTitle = "YouTube Track $videoId"
                    val cleaned = MetadataCleaner.cleanMetadata(fallbackTitle)
                    
                    _uiState.value = VibeUiState.MetadataReady(
                        url = trimmedUrl,
                        rawTitle = fallbackTitle,
                        title = cleaned.title,
                        artist = cleaned.artist,
                        albumArtIndex = cleaned.albumArtIndex,
                        streamUrl = "" // No hay URL de descarga directa si no está en la DB
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = VibeUiState.Error("Fallo de conexión: ${e.localizedMessage ?: "Error desconocido en Supabase"}")
            }
        }
    }

    /**
     * Transfiere el trabajo pesado a la capa del sistema operativo mediante WorkManager.
     */
    fun startDownloadAndConversion(context: Context, title: String, artist: String, albumArtIndex: Int, streamUrl: String) {
        if (streamUrl.isEmpty()) {
            _uiState.value = VibeUiState.Error("Este video aún no ha sido procesado por el servidor y no tiene enlace de descarga.")
            return
        }

        val videoId = IntentParser.extractVideoId(_pastedUrl.value) ?: "unknown_id"
        
        val inputData = workDataOf(
            "VIDEO_ID" to videoId,
            "TITLE" to title,
            "ARTIST" to artist,
            "STREAM_URL" to streamUrl
        )

        val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "download_$videoId",
            ExistingWorkPolicy.KEEP,
            downloadRequest
        )

        _uiState.value = VibeUiState.Completed(title, artist, Uri.EMPTY, albumArtIndex)
    }

    fun playDownloadedSong(context: Context, fileUri: Uri) {
        try {
            val playIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, "audio/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(playIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetState() {
        _uiState.value = VibeUiState.Idle
    }
}
