package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.VibeTuneViewModel
import com.example.ui.screens.VibeUiState
import com.example.ui.components.CapturingStateView
import com.example.ui.components.CompletedStateView
import com.example.ui.components.ConvertingStateView
import com.example.ui.components.ErrorStateView
import com.example.ui.components.MetadataReadyView
import com.example.ui.theme.FrostBorder
import com.example.ui.theme.FrostSurface
import com.example.ui.theme.VibeTextSecondary

@Composable
fun ShareSheetOverlay(
    viewModel: VibeTuneViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x9908070D)) // Deep translucent dimming
            .clickable(onClick = onDismiss), // Tap empty space to close
        contentAlignment = Alignment.BottomCenter
    ) {
        // Stop clicks inside the sheet from dismissing it
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(FrostSurface.copy(alpha = 0.95f))
                .border(
                    width = 1.dp,
                    color = FrostBorder,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
                .clickable(enabled = false) {} // block clickthrough
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag handle decorator
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0x339E9CAE))
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sheet State Switcher
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    slideInVertically { height -> height } + fadeIn() togetherWith
                    slideOutVertically { height -> -height } + fadeOut()
                },
                label = "SheetStateChange"
            ) { state ->
                when (state) {
                    is VibeUiState.Idle -> {
                        Text(
                            text = "Waiting for input...",
                            color = VibeTextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    is VibeUiState.Capturing -> {
                        CapturingStateView()
                    }

                    is VibeUiState.MetadataReady -> {
                        MetadataReadyView(
                            title = state.title,
                            artist = state.artist,
                            albumArtIndex = state.albumArtIndex,
                            onDownloadStart = {
                                viewModel.startDownloadAndConversion(
                                    context, state.title, state.artist, state.albumArtIndex, state.streamUrl
                                )
                            },
                            onCancel = onDismiss
                        )
                    }

                    is VibeUiState.Converting -> {
                        ConvertingStateView(
                            title = state.title,
                            artist = state.artist,
                            progress = state.progress,
                            albumArtIndex = state.albumArtIndex
                        )
                    }

                    is VibeUiState.Completed -> {
                        CompletedStateView(
                            title = state.title,
                            artist = state.artist,
                            albumArtIndex = state.albumArtIndex,
                            onPlay = { viewModel.playDownloadedSong(context, state.fileUri) },
                            onDone = onDismiss
                        )
                    }

                    is VibeUiState.Error -> {
                        ErrorStateView(
                            message = state.message,
                            onDismiss = onDismiss
                        )
                    }
                }
            }
        }
    }
}
