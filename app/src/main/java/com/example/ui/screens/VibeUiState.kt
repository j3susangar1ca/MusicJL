package com.example.ui.screens

import android.net.Uri

sealed interface VibeUiState {
    object Idle : VibeUiState
    object Capturing : VibeUiState
    
    data class MetadataReady(
        val url: String,
        val rawTitle: String,
        val title: String,
        val artist: String,
        val albumArtIndex: Int,
        val streamUrl: String
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
