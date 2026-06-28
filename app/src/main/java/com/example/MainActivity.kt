package com.example

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check if activity was launched via YouTube "Share" sheet
        val intentAction = intent?.action
        val intentType = intent?.type
        val isShareSheetFlow = intentAction == Intent.ACTION_SEND && intentType == "text/plain"
        val sharedText = intent?.getStringExtra(Intent.EXTRA_TEXT) ?: ""

        setContent {
            MyApplicationTheme {
                val viewModel: VibeTuneViewModel = viewModel()
                val context = LocalContext.current

                // Trigger processing automatically if shared
                LaunchedEffect(intentAction) {
                    viewModel.refreshHistory(context)
                    if (isShareSheetFlow && sharedText.isNotEmpty()) {
                        viewModel.processUrl(context, sharedText)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (isShareSheetFlow) Color.Transparent else MaterialTheme.colorScheme.background
                ) {
                    if (isShareSheetFlow) {
                        // Translucent Share Sheet Flow (Floating over YouTube)
                        ShareSheetOverlay(
                            viewModel = viewModel,
                            onDismiss = { finish() }
                        )
                    } else {
                        // Fully immersive dark dashboard for manual conversions and history
                        MainDashboardScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

/**
 * ShareSheetOverlay dims the YouTube app background and overlays a sleek, translucent M3 sheet at the bottom.
 */
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
                        // Placeholder
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
                                    context, state.title, state.artist, state.albumArtIndex
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

/**
 * Standard launcher dashboard for managing offline tracks and pasting links manually.
 */
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

// ==========================================
// Sub-views for state transitions
// ==========================================

@Composable
fun CapturingStateView() {
    val infiniteTransition = rememberInfiniteTransition(label = "PulsingGlow")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulsingScale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulsingAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .scale(scale)
                .size(80.dp)
                .background(FrostLavender.copy(alpha = 0.15f), CircleShape)
                .border(2.dp, FrostLavender.copy(alpha = alpha), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Capturando enlace",
                tint = FrostLavender,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Capturando enlace...",
            color = FrostTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Consultando servidores de conversión de Music JL",
            color = FrostTextSecondary,
            fontSize = 13.sp
        )
    }
}

@Composable
fun MetadataReadyView(
    title: String,
    artist: String,
    albumArtIndex: Int,
    onDownloadStart: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(FrostLavender, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "PROCESAMIENTO INTELIGENTE",
                color = FrostLavender,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Premium Horizontally styled Metadata card matching the HTML design
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(FrostCardBg)
                .border(1.dp, Color(0x0AFFFFFF), RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlbumArtPlaceholder(
                index = albumArtIndex,
                isPlaying = false,
                size = 80.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = artist,
                    color = FrostTextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Clean Metadata Badge
                Box(
                    modifier = Modifier
                        .background(FrostLavender.copy(alpha = 0.1f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "METADATOS LIMPIOS",
                        color = FrostLavender,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .testTag("cancel_sync_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = FrostTextSecondary),
                border = BorderStroke(1.dp, FrostBorder),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Cancelar", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onDownloadStart,
                modifier = Modifier
                    .weight(1.5f)
                    .height(54.dp)
                    .testTag("start_download_button"),
                colors = ButtonDefaults.buttonColors(containerColor = FrostLavender),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = FrostDarkPurple,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Iniciar descarga", color = FrostDarkPurple, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ConvertingStateView(
    title: String,
    artist: String,
    progress: Int,
    albumArtIndex: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(FrostLavender, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CONVIRTIENDO... $progress%",
                color = FrostLavender,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Horizontal metadata card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(FrostCardBg)
                .border(1.dp, Color(0x0AFFFFFF), RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlbumArtPlaceholder(
                index = albumArtIndex,
                isPlaying = true, // Spin!
                size = 64.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = artist,
                    color = FrostTextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Slim glowing frosted linear progress bar
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Convirtiendo a MP3 de 320kbps...",
                    color = FrostTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$progress%",
                    color = FrostLavender,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color(0x1AFFFFFF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress / 100f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(FrostLavender)
                )
            }
        }
    }
}

@Composable
fun CompletedStateView(
    title: String,
    artist: String,
    albumArtIndex: Int,
    onPlay: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color(0x2234C759), CircleShape)
                .border(2.dp, Color(0xFF34C759), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Marca de éxito",
                tint = Color(0xFF34C759),
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¡Guardado perfectamente!",
            color = Color(0xFF34C759),
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        AlbumArtPlaceholder(
            index = albumArtIndex,
            isPlaying = false,
            size = 100.dp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = title,
            color = FrostTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = artist,
            color = FrostTextSecondary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onPlay,
                modifier = Modifier
                    .weight(1.2f)
                    .height(54.dp)
                    .testTag("play_now_button"),
                colors = ButtonDefaults.buttonColors(containerColor = FrostCardBg),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, FrostLavender.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Reproducir pista ahora",
                    tint = FrostLavender,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Escuchar ahora", color = FrostTextPrimary, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onDone,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .testTag("done_close_button"),
                colors = ButtonDefaults.buttonColors(containerColor = FrostLavender),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Hecho", color = FrostDarkPurple, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ErrorStateView(
    message: String,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(Color(0x33FF2D55), CircleShape)
                .border(2.dp, Color(0xFFFF2D55), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Logotipo de advertencia de error",
                tint = Color(0xFFFF2D55),
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Descarga interrumpida",
            color = Color(0xFFFF2D55),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = message,
            color = FrostTextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(54.dp)
                .testTag("error_close_button"),
            colors = ButtonDefaults.buttonColors(containerColor = FrostCardBg),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, FrostBorder)
        ) {
            Text("Volver", color = FrostTextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * AlbumArtPlaceholder draws a gorgeous high-fidelity procedurally rotating Vinyl disk record with customizable modern gradients.
 */
@Composable
fun AlbumArtPlaceholder(
    index: Int,
    isPlaying: Boolean,
    size: androidx.compose.ui.unit.Dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "VinylSpin")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SpinAngle"
    )

    val currentRotation = if (isPlaying) rotationAngle else 0f

    // High fidelity linear gradient colors representing visual mood
    val gradientColors = when (index) {
        0 -> listOf(Color(0xFF00C7BE), Color(0xFF5856D6)) // Cyan to Indigo
        1 -> listOf(Color(0xFFFF2D55), Color(0xFFFF9500)) // Rose to Orange
        2 -> listOf(Color(0xFFFFCC00), Color(0xFF34C759)) // Gold to Emerald
        3 -> listOf(Color(0xFFAF52DE), Color(0xFFFF2D55)) // Purple to Fuchsia
        4 -> listOf(Color(0xFF007AFF), Color(0xFF00C7BE)) // Sapphire to Cyan
        else -> listOf(Color(0xFFF1A80A), Color(0xFFE02424)) // Amber to Crimson
    }

    Box(
        modifier = Modifier
            .size(size)
            .rotate(currentRotation)
            .clip(CircleShape)
            .background(Color(0xFF08070D))
            .border(2.dp, Color(0x1AFFFFFF), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Draw vinyl lines using concentric circular overlays
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = center
            val radius = size.toPx() / 2
            
            // Draw Vinyl Grooves
            drawCircle(
                color = Color(0x1F2B2B33),
                radius = radius * 0.85f,
                center = center,
                style = strokeStyle(1.5f)
            )
            drawCircle(
                color = Color(0x11FFFFFF),
                radius = radius * 0.75f,
                center = center,
                style = strokeStyle(1f)
            )
            drawCircle(
                color = Color(0x1F2B2B33),
                radius = radius * 0.65f,
                center = center,
                style = strokeStyle(1.5f)
            )
            drawCircle(
                color = Color(0x0EFFFFFF),
                radius = radius * 0.55f,
                center = center,
                style = strokeStyle(1f)
            )
        }

        // Central visual gradient disc representing album label
        Box(
            modifier = Modifier
                .fillMaxSize(0.48f)
                .clip(CircleShape)
                .background(
                    brush = Brush.sweepGradient(
                        colors = gradientColors
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Inner core metallic spindle cutout hole
            Box(
                modifier = Modifier
                    .fillMaxSize(0.24f)
                    .clip(CircleShape)
                    .background(Color(0xFF08070D))
                    .border(1.dp, Color(0xFF9E9CAE), CircleShape)
            )
        }
    }
}

// Support helper for drawing simple hairline strokes
private fun strokeStyle(width: Float) = androidx.compose.ui.graphics.drawscope.Stroke(
    width = width,
    pathEffect = null
)
