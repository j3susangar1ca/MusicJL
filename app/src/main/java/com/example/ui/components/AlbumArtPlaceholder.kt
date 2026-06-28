package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Renderiza una carátula de álbum simulada mediante gradientes premium algorítmicos.
 * Resuelve el diseño dinámico basado en el hash del título calculado en MetadataCleaner.
 */
@Composable
fun AlbumArtPlaceholder(
    index: Int,
    size: Dp = 120.dp
) {
    // Paleta de gradientes audiófilos (0-5) basados en tendencias de interfaces premium
    val gradients = listOf(
        // 0: Sunset Glow (Naranja vibrante a Rosa místico)
        Brush.linearGradient(listOf(Color(0xFFFF5722), Color(0xFFE91E63))),
        // 1: Deep Ocean (Azul profundo a Turquesa eléctrico)
        Brush.linearGradient(listOf(Color(0xFF00C9FF), Color(0xFF92FE9D))),
        // 2: Neon Midnight (Púrpura cibernético a Violeta oscuro)
        Brush.linearGradient(listOf(Color(0xFF8A2387), Color(0xFFE94057))),
        // 3: Emerald Forest (Verde esmeralda a Menta suave)
        Brush.linearGradient(listOf(Color(0xFF11998e), Color(0xFF38ef7d))),
        // 4: Electric Cyber (Amarillo neón a Magenta)
        Brush.linearGradient(listOf(Color(0xFFF12711), Color(0xFFF5AF19))),
        // 5: Cosmic Nebula (Índigo espacial a Orquídea)
        Brush.linearGradient(listOf(Color(0xFF6441A5), Color(0xFF2a0845)))
    )

    // Escudo contra índices fuera de rango (seguridad técnica)
    val selectedGradient = gradients.getOrElse(index) { gradients[0] }

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp)) // Esquinas redondeadas estilo Material 3
            .background(selectedGradient),
        contentAlignment = Alignment.Center
    ) {
        // Ícono de nota musical minimalista y translúcido integrado con el tema
        Icon(
            imageVector = Icons.Filled.MusicNote,
            contentDescription = "Music Note Placeholder",
            modifier = Modifier.size(size * 0.45f), // Escalado proporcional al tamaño del contenedor
            tint = Color.White.copy(alpha = 0.85f)
        )
    }
}