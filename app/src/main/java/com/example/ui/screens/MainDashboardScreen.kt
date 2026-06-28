package com.example.ui.screens

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.VibeTuneViewModel
import com.example.VibeUiState
import com.example.ui.components.AlbumArtPlaceholder
import com.example.ui.components.CapturingStateView
import com.example.ui.components.CompletedStateView
import com.example.ui.components.ConvertingStateView
import com.example.ui.components.ErrorStateView
import com.example.ui.components.MetadataReadyView
import com.example.ui.theme.FrostBorder
import com.example.ui.theme.FrostCardBg
import com.example.ui.theme.FrostDarkBackground
import com.example.ui.theme.FrostDarkPurple
import com.example.ui.theme.FrostLavender
import com.example.ui.theme.FrostSurface
import com.example.ui.theme.FrostTextPrimary
import com.example.ui.theme.FrostTextSecondary

@Composable
fun MainDashboardScreen(
    viewModel: VibeTuneViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pastedUrl by viewModel.pastedUrl.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var clipboardText by remember { mutableStateOf("") }

    // Read clipboard content
    LaunchedEffect(Unit) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        if (clipboard != null && clipboard.hasPrimaryClip() && 
            clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true) {
            val item = clipboard.primaryClip?.getItemAt(0)
            val text = item?.text?.toString() ?: ""
            if (text.contains("youtube.com") || text.contains("youtu.be")) {
                clipboardText = text
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FrostDarkBackground)
            .drawBehind {
                val w = this.size.width
                val h = this.size.height
                // Elite, glowing radial ambient lighting
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(FrostLavender.copy(alpha = 0.16f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(w * 0.9f, h * 0.15f),
                        radius = w * 0.7f
                    ),
                    radius = w * 0.7f,
                    center = androidx.compose.ui.geometry.Offset(w * 0.9f, h * 0.15f)
                )
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF5856D6).copy(alpha = 0.14f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(w * 0.1f, h * 0.85f),
                        radius = w * 0.8f
                    ),
                    radius = w * 0.8f,
                    center = androidx.compose.ui.geometry.Offset(w * 0.1f, h * 0.85f)
                )
            }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MUSIC JL",
                                color = FrostLavender,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.5.sp,
                                fontFamily = FontFamily.SansSerif
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "El compañero invisible para MP3",
                                color = FrostTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.2.sp
                            )
                        }

                        IconButton(
                            onClick = { viewModel.refreshHistory(context) },
                            modifier = Modifier
                                .testTag("refresh_button")
                                .background(FrostSurface.copy(alpha = 0.6f), CircleShape)
                                .border(1.dp, FrostBorder, CircleShape)
                                .size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Actualizar lista de historial",
                                tint = FrostLavender,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                ) {
                    // Link Capture Card with advanced Glassmorphic depth
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = FrostSurface.copy(alpha = 0.55f)),
                        shape = RoundedCornerShape(26.dp),
                        border = BorderStroke(1.dp, FrostBorder)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Extraer nuevo MP3",
                                color = FrostTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = pastedUrl,
                                onValueChange = { viewModel.updatePastedUrl(it) },
                                placeholder = { Text("Pegar enlace de YouTube Music...", color = FrostTextSecondary, fontSize = 14.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = FrostTextPrimary,
                                    unfocusedTextColor = FrostTextPrimary,
                                    focusedBorderColor = FrostLavender,
                                    unfocusedBorderColor = FrostBorder,
                                    focusedLabelColor = FrostLavender
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("url_input_field"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.processUrl(context, pastedUrl) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("extract_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = FrostLavender),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Extraer enlace de audio",
                                        tint = FrostDarkPurple,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Procesar", color = FrostDarkPurple, fontWeight = FontWeight.Bold)
                                }

                                // Dynamic Auto-Paste suggestion
                                if (clipboardText.isNotEmpty()) {
                                    Button(
                                        onClick = {
                                            viewModel.updatePastedUrl(clipboardText)
                                            viewModel.processUrl(context, clipboardText)
                                        },
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .height(48.dp)
                                            .testTag("autopaste_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = FrostCardBg),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, FrostLavender.copy(alpha = 0.4f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = "Pegar automáticamente del portapapeles",
                                            tint = FrostLavender,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Pegar enlace",
                                            color = FrostTextPrimary,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Saved Music List Section
                    Text(
                        text = "Biblioteca Music JL",
                        color = FrostTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (historyList.isEmpty()) {
                        // Friendly instructional empty state
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(FrostSurface, CircleShape)
                                    .border(1.dp, FrostBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Logotipo de instrucciones",
                                    tint = FrostTextSecondary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Aún no hay canciones capturadas",
                                color = FrostTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "¡Busca una canción en YouTube Music, toca 'Compartir' y elige Music JL para guardar MP3s sin conexión al instante! Sin necesidad de abrir la aplicación.",
                                color = FrostTextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .testTag("download_history_list"),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(historyList, key = { it.id }) { song ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(FrostSurface)
                                        .border(1.dp, FrostBorder, RoundedCornerShape(16.dp))
                                        .clickable { viewModel.playDownloadedSong(context, song.uri) }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AlbumArtPlaceholder(
                                        index = Math.abs(song.title.hashCode() % 6),
                                        isPlaying = false,
                                        size = 48.dp
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = song.title,
                                            color = FrostTextPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = song.artist,
                                            color = FrostTextSecondary,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(FrostCardBg, CircleShape)
                                            .border(1.dp, FrostLavender.copy(alpha = 0.3f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play Track",
                                            tint = FrostLavender,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Overlay modal processing layout in manual mode too!
                if (uiState !is VibeUiState.Idle) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xCC08070D))
                            .clickable { viewModel.resetState() },
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .clickable(enabled = false) {},
                            colors = CardDefaults.cardColors(containerColor = FrostSurface),
                            shape = RoundedCornerShape(28.dp),
                            border = BorderStroke(1.dp, FrostBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AnimatedContent(
                                    targetState = uiState,
                                    label = "ModalState"
                                ) { state ->
                                    when (state) {
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
                                                        context, state.title, state.artist, state.albumArtIndex
                                                    )
                                                },
                                                onCancel = { viewModel.resetState() }
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
                                                onDone = { viewModel.resetState() }
                                            )
                                        }

                                        is VibeUiState.Error -> {
                                            ErrorStateView(
                                                message = state.message,
                                                onDismiss = { viewModel.resetState() }
                                            )
                                        }

                                        else -> {}
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
