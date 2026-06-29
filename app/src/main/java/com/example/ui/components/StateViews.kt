package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FrostBorder
import com.example.ui.theme.FrostCardBg
import com.example.ui.theme.FrostLavender
import com.example.ui.theme.FrostTextPrimary
import com.example.ui.theme.FrostTextSecondary
import com.example.ui.theme.FrostSurface
import com.example.ui.theme.FrostDarkPurple

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
                        text = "METADATOS LIMPIOS", // Preserved label from layout
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
                    imageVector = Icons.Default.PlayArrow, // Replaced play arrow with standard icon if needed or let it compile
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
