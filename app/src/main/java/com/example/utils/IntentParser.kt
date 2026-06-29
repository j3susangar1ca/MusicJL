package com.example.utils

import android.content.Intent
import java.util.regex.Pattern

/**
 * Contenedor con la información básica extraída de la acción de compartir.
 */
data class ParsedYoutubeInfo(
    val videoId: String,       // El identificador único de 11 caracteres de YouTube
    val cleanUrl: String,      // URL normalizada y estandarizada para enviar al backend
    val rawTitle: String       // El título crudo rescatado del texto compartido
)

/**
 * Utilidad encargada de procesar e interceptar los Intents del sistema de Android.
 */
object IntentParser {

    // Expresión regular robusta para capturar el ID de video de YouTube (soporta youtube.com, youtu.be, music.youtube, etc.)
    private val youtubeIdRegex = "(?:youtube\\.com\\/(?:[^\\/\\n\\s]+\\/\\S+\\/|(?:v|e(?:mbed)?|shorts)\\/|\\S*?[?&]v=)|music\\.youtube\\.com\\/watch\\?v=|youtu\\.be\\/)([a-zA-Z0-9_-]{11})".toRegex()

    /**
     * Extrae únicamente el ID de video de 11 caracteres de una URL.
     */
    fun extractVideoId(url: String): String? {
        val matchResult = youtubeIdRegex.find(url)
        return matchResult?.groupValues?.get(1)
    }

    fun parseVideoId(url: String): String? = extractVideoId(url)

    /**
     * Analiza el Intent entrante. Si es un link de YouTube válido, extrae sus componentes.
     * 
     * @param intent El Intent capturado en la MainActivity
     * @return Objeto [ParsedYoutubeInfo] estructurado, o null si el formato no es compatible.
     */
    fun parseShareIntent(intent: Intent?): ParsedYoutubeInfo? {
        // Validar que el Intent provenga del menú "Compartir" y contenga texto plano
        if (intent == null || intent.action != Intent.ACTION_SEND || intent.type != "text/plain") {
            return null
        }

        // Recuperar el texto completo compartido por YouTube
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return null

        // Buscar el ID del video mediante coincidencia Regex
        val matchResult = youtubeIdRegex.find(sharedText) ?: return null
        val videoId = matchResult.groupValues[1]
        val cleanUrl = "https://www.youtube.com/watch?v=$videoId"

        // Extraer y limpiar el título crudo removiendo la URL y marcas de agua de la plataforma
        val rawTitle = sharedText
            .replace(matchResult.value, "")                  // Removemos el enlace físico
            .replace("Mira este video en YouTube:", "")       // Limpieza si viene de YouTube estándar
            .replace("Escucha este tema en YouTube Music:", "") // Limpieza si viene de YT Music
            .replace(" - YouTube Music", "")                // Coletillas adicionales
            .replace(" - YouTube", "")
            .trim()
            .ifEmpty { "YouTube Track $videoId" }            // Respaldo en caso de que quede vacío

        return ParsedYoutubeInfo(
            videoId = videoId,
            cleanUrl = cleanUrl,
            rawTitle = rawTitle
        )
    }
}