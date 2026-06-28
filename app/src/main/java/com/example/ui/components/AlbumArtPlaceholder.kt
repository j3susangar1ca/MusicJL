package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

@Composable
fun AlbumArtPlaceholder(
    index: Int,
    isPlaying: Boolean,
    size: Dp
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
