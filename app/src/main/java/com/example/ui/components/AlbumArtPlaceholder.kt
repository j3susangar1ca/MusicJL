package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Placeholder algorítmico premium adaptado para soportar animaciones 
 * de rotación asíncronas basadas en el estado de conversión de la pista.
 */
@Composable
fun AlbumArtPlaceholder(
    index: Int,
    isPlaying: Boolean,
    size: Dp = 80.dp
) {
    // Animación de rotación infinita para simular el procesamiento físico del audio
    val infiniteTransition = rememberInfiniteTransition(label = "VinylRotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "VinylAngle"
    )

    // Paleta de degradados vibrantes que actúan como carátula procedimental
    val gradients = listOf(
        Brush.linearGradient(listOf(Color(0xFF8A2387), Color(0xFFE94057), Color(0xFFF27121))),
        Brush.linearGradient(listOf(Color(0xFF00C6FF), Color(0xFF0072FF))),
        Brush.linearGradient(listOf(Color(0xFF11998E), Color(0xFF38EF7D))),
        Brush.linearGradient(listOf(Color(0xFFF12711), Color(0xFFF5AF19))),
        Brush.linearGradient(listOf(Color(0xFF6441A5), Color(0xFF2A0845))),
        Brush.linearGradient(listOf(Color(0xFFFE8C00), Color(0xFFF83600)))
    )
    val currentGradient = gradients.getOrElse(index) { gradients[0] }

    // Aplicar la rotación únicamente si la máquina de estados indica que está convirtiendo
    val dynamicModifier = if (isPlaying) Modifier.rotate(angle) else Modifier

    Box(
        modifier = Modifier
            .size(size)
            .then(dynamicModifier)
            .clip(CircleShape) // Forma circular de disco audiófilo premium
            .background(currentGradient),
        contentAlignment = Alignment.Center
    ) {
        // Anillo interior translúcido estilo vinilo profesional
        Box(
            modifier = Modifier
                .size(size * 0.35f)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.25f))
        )
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(size * 0.4f)
        )
    }
}