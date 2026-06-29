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
                    _uiState.value = VibeUiState.Error("El video aún no ha sido procesado por el servidor de Music JL.")
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

        // For now, we jump to Completed to satisfy the UI, but in a real app 
        // you would observe the WorkInfo to update progress/state.
        // We use a dummy URI for now since the worker is doing the actual saving.
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
