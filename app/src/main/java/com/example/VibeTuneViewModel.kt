package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Screen states for the VibeTune UI
sealed interface VibeUiState {
    object Idle : VibeUiState
    object Capturing : VibeUiState
    
    data class MetadataReady(
        val url: String,
        val rawTitle: String,
        val title: String,
        val artist: String,
        val albumArtIndex: Int
    ) : VibeUiState
    
    data class Converting(
        val title: String,
        val artist: String,
        val progress: Int,
        val albumArtIndex: Int
    ) : VibeUiState
    
    data class Completed(
        val title: String,
        val artist: String,
        val fileUri: Uri,
        val albumArtIndex: Int
    ) : VibeUiState
    
    data class Error(val message: String) : VibeUiState
}

class VibeTuneViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<VibeUiState>(VibeUiState.Idle)
    val uiState: StateFlow<VibeUiState> = _uiState.asStateFlow()

    private val _pastedUrl = MutableStateFlow("")
    val pastedUrl: StateFlow<String> = _pastedUrl.asStateFlow()

    private val _historyList = MutableStateFlow<List<DownloadedSong>>(emptyList())
    val historyList: StateFlow<List<DownloadedSong>> = _historyList.asStateFlow()

    // Mock database mapping YouTube video IDs to raw titles
    private val mockYoutubeDatabase = mapOf(
        "coYw-M7X6M0" to "Lady Gaga, Bruno Mars - Die With A Smile (Official Music Video)",
        "d_HlPboLRL8" to "Sabrina Carpenter - Espresso [HD Official Audio]",
        "2Tz8N0_3g9Y" to "Billie Eilish - BIRDS OF A FEATHER (Lyrics)",
        "PinkPony" to "Chappell Roan - Pink Pony Club [Official Lyric Video]",
        "Flowers" to "Miley Cyrus - Flowers (Official Video)",
        "Starboy" to "The Weeknd ft. Daft Punk - Starboy [HQ 1080p]"
    )

    fun updatePastedUrl(url: String) {
        _pastedUrl.value = url
    }

    /**
     * Resets the download status back to Idle (for main screen dashboard).
     */
    fun resetState() {
        _uiState.value = VibeUiState.Idle
    }

    /**
     * Queries saved files from MediaStore to populate dashboard list.
     */
    fun refreshHistory(context: Context) {
        viewModelScope.launch {
            val songs = MediaStoreHelper.getDownloadedVibeTuneSongs(context)
            _historyList.value = songs
        }
    }

    /**
     * Main handler that processes incoming links, whether shared from YouTube Music
     * or manually pasted in the dashboard.
     */
    fun processUrl(context: Context, url: String) {
        val trimmedUrl = url.trim()
        if (trimmedUrl.isEmpty()) {
            _uiState.value = VibeUiState.Error("Por favor, introduce un enlace de YouTube válido.")
            return
        }

        viewModelScope.launch {
            _uiState.value = VibeUiState.Capturing
            // Feel-good pulsing animation sleep (1200ms)
            delay(1200)

            // Validate if it is a plausible YouTube or general link
            val isYoutube = trimmedUrl.contains("youtube.com", ignoreCase = true) || 
                            trimmedUrl.contains("youtu.be", ignoreCase = true) ||
                            trimmedUrl.contains("http://", ignoreCase = true) ||
                            trimmedUrl.contains("https://", ignoreCase = true)

            if (!isYoutube) {
                _uiState.value = VibeUiState.Error("Fuente de audio no válida. Por favor, comparte un enlace válido de YouTube o YouTube Music.")
                return@launch
            }

            // Extract a title from our database, or infer from URL query parameter, or assign a beautiful default
            var rawTitle = "Lady Gaga, Bruno Mars - Die With A Smile (Official Music Video)" // Gorgeous default
            
            // Try to match mock database keys
            for ((key, title) in mockYoutubeDatabase) {
                if (trimmedUrl.contains(key, ignoreCase = true)) {
                    rawTitle = title
                    break
                }
            }

            // If not found in mock database but URL contains custom text hints
            if (rawTitle == mockYoutubeDatabase.values.first() && !trimmedUrl.contains("coYw-M7X6M0")) {
                if (trimmedUrl.contains("espresso", ignoreCase = true)) {
                    rawTitle = mockYoutubeDatabase["d_HlPboLRL8"]!!
                } else if (trimmedUrl.contains("feather", ignoreCase = true) || trimmedUrl.contains("billie", ignoreCase = true)) {
                    rawTitle = mockYoutubeDatabase["2Tz8N0_3g9Y"]!!
                } else if (trimmedUrl.contains("flowers", ignoreCase = true) || trimmedUrl.contains("miley", ignoreCase = true)) {
                    rawTitle = mockYoutubeDatabase["Flowers"]!!
                } else if (trimmedUrl.contains("starboy", ignoreCase = true) || trimmedUrl.contains("weeknd", ignoreCase = true)) {
                    rawTitle = mockYoutubeDatabase["Starboy"]!!
                } else {
                    // Procedural generation based on url string to make it fun
                    val lengthValue = trimmedUrl.length % 4
                    rawTitle = when (lengthValue) {
                        0 -> "Chappell Roan - Pink Pony Club [Official Lyric Video]"
                        1 -> "Post Malone, Morgan Wallen - I Had Some Help (Official Video)"
                        2 -> "Kendrick Lamar - Not Like Us [Official Audio]"
                        else -> "Music JL Synth Beats - Sesión Lofi de Medianoche (1080p HD)"
                    }
                }
            }

            // Perform metadata cleaning via the simulated Gemini backend cleaner
            val cleanedMetadata = MetadataCleaner.cleanMetadata(rawTitle)

            _uiState.value = VibeUiState.MetadataReady(
                url = trimmedUrl,
                rawTitle = rawTitle,
                title = cleanedMetadata.title,
                artist = cleanedMetadata.artist,
                albumArtIndex = cleanedMetadata.albumArtIndex
            )
        }
    }

    /**
     * Starts the simulated YouTube-to-MP3 extraction and conversion,
     * updating progress sequentially, saving the file, and refreshing history.
     */
    fun startDownloadAndConversion(context: Context, title: String, artist: String, albumArtIndex: Int) {
        val currentState = _uiState.value
        if (currentState !is VibeUiState.MetadataReady) return

        viewModelScope.launch {
            _uiState.value = VibeUiState.Converting(title, artist, 0, albumArtIndex)

            // Progressive conversion feedback
            val progressSteps = listOf(12, 28, 45, 61, 79, 93, 100)
            for (progress in progressSteps) {
                delay(350) // Sleek conversions need a small moment of realism
                _uiState.value = VibeUiState.Converting(title, artist, progress, albumArtIndex)
            }

            // Save the dummy `.mp3` file to Shared Music/VibeTune folder
            val savedUri = MediaStoreHelper.saveMp3ToMusicFolder(context, title, artist)

            if (savedUri != null) {
                _uiState.value = VibeUiState.Completed(title, artist, savedUri, albumArtIndex)
                // Refresh our history database immediately
                refreshHistory(context)
            } else {
                _uiState.value = VibeUiState.Error("No se pudo registrar el archivo MP3 en la biblioteca de medios del sistema.")
            }
        }
    }

    /**
     * Triggers a system view action intent to play the downloaded music track in the default player.
     */
    fun playDownloadedSong(context: Context, fileUri: Uri) {
        try {
            val playIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, "audio/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(playIntent)
        } catch (e: Exception) {
            // Fallback error if no player is found or media intent fails
            e.printStackTrace()
        }
    }
}
