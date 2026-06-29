package com.example.ui.screens

sealed interface VibeUiState {
    object Idle : VibeUiState
    object Capturing : VibeUiState
    object Completed : VibeUiState
    
    data class MetadataReady(
        val url: String,
        val rawTitle: String,
        val title: String,
        val artist: String,
        val albumArtIndex: Int,
        val streamUrl: String
    ) : VibeUiState
    
    data class Error(val message: String) : VibeUiState
}
