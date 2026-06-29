package com.example

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.data.remote.SupabaseClient
import com.example.ui.screens.VibeUiState
import com.example.utils.IntentParser
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

    fun updatePastedUrl(url: String) {
        _pastedUrl.value = url
    }

    /**
     * Intercepta el enlace, consulta Supabase en tiempo real y parsea la respuesta.
     */
    fun processUrl(context: Context, url: String) {
        val trimmedUrl = url.trim()
        if (trimmedUrl.isEmpty()) return

        // Extraer el identificador nativo de YouTube usando tu Utilitario
        val videoId = IntentParser.extractVideoId(trimmedUrl)
        if (videoId.isNullOrEmpty()) {
            _uiState.value = VibeUiState.Error("La URL proporcionada no es un enlace válido de YouTube.")
            return
        }

        viewModelScope.launch {
            _uiState.value = VibeUiState.Capturing // Despierta tu CapturingStateView frosted premium
            try {
                // Petición HTTP directa a tu base de datos Cloud
                val responseList = SupabaseClient.apiService.getConvertedTrackInfo(videoId = videoId)
                
                if (responseList.isNotEmpty()) {
                    val track = responseList.first()
                    
                    // Ejecuta tu limpiador algorítmico determinista
                    val cleaned = MetadataCleaner.cleanMetadata(track.title)

                    // Cambia al estado de Confirmación con datos reales del backend
                    _uiState.value = VibeUiState.MetadataReady(
                        url = trimmedUrl,
                        rawTitle = track.title,
                        title = cleaned.title,
                        artist = track.artist.ifBlank { cleaned.artist },
                        albumArtIndex = cleaned.albumArtIndex,
                        streamUrl = track.downloadUrl // Pasamos el stream HTTP real al estado
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
     * Inmune a cierres de la aplicación.
     */
    fun startDownloadAndConversion(context: Context, title: String, artist: String, streamUrl: String) {
        val videoId = IntentParser.extractVideoId(_pastedUrl.value) ?: "unknown_id"
        
        // Empaquetar los metadatos reales para el hilo de fondo
        val inputData = workDataOf(
            "VIDEO_ID" to videoId,
            "TITLE" to title,
            "ARTIST" to artist,
            "STREAM_URL" to streamUrl
        )

        val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(inputData)
            .build()

        // Encolar de manera única para evitar descargas duplicadas de la misma canción
        WorkManager.getInstance(context).enqueueUniqueWork(
            "download_$videoId",
            ExistingWorkPolicy.KEEP,
            downloadRequest
        )

        // Sincronizar el estado de la UI directamente a completado tras delegar al worker
        _uiState.value = VibeUiState.Completed
    }

    fun resetState() {
        _uiState.value = VibeUiState.Idle
    }
}
