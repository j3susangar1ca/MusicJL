package com.example

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File

// Data model representing a downloaded MP3 track
data class DownloadedSong(
    val id: Long,
    val title: String,
    val artist: String,
    val uri: Uri
)

// Cleaned metadata parsed from raw YouTube title
data class SongMetadata(
    val title: String,
    val artist: String,
    val albumArtIndex: Int
)

/**
 * MetadataCleaner cleans raw YouTube titles to isolate the song name and artist.
 * It simulates a serverless backend cleaner, or a call to Gemini.
 */
object MetadataCleaner {

    /**
     * Isolates song title and artist, stripping unwanted YouTube clutter.
     */
    fun cleanMetadata(rawTitle: String): SongMetadata {
        var artist = ""
        var title = ""
        
        // Split title on common delimiters: " - ", " – ", " | ", " : ", "-"
        val delimiters = listOf(" - ", " – ", " | ", " : ", "-")
        var splitFound = false
        for (delim in delimiters) {
            if (rawTitle.contains(delim)) {
                val parts = rawTitle.split(delim, limit = 2)
                artist = parts[0].trim()
                title = parts[1].trim()
                splitFound = true
                break
            }
        }
        
        if (!splitFound) {
            title = rawTitle.trim()
            artist = "Especial de Music JL"
        }
        
        // Clean each part
        title = cleanGarbage(title)
        artist = cleanGarbage(artist)
        
        // Check for "feat." or "ft." in artist
        val featKeywords = listOf(" ft. ", " feat. ", " ft ", " feat ")
        for (feat in featKeywords) {
            val lowerArtist = artist.lowercase()
            val lowerFeat = feat.lowercase()
            if (lowerArtist.contains(lowerFeat)) {
                val index = lowerArtist.indexOf(lowerFeat)
                val primaryArtist = artist.substring(0, index).trim()
                val featuredPart = artist.substring(index + feat.length).trim()
                artist = primaryArtist
                // Append feature to title
                if (featuredPart.isNotEmpty()) {
                    title = "$title (feat. $featuredPart)"
                }
                break
            }
        }
        
        // Fallbacks for empty titles or artists
        if (title.isEmpty()) title = "Canción desconocida"
        if (artist.isEmpty() || artist == "Especial de Music JL") artist = "Artista de Music JL"
        
        // Assign a deterministic album art gradient index (0-5) based on title hash
        val albumArtIndex = Math.abs(title.hashCode() % 6)
        
        return SongMetadata(title, artist, albumArtIndex)
    }
    
    /**
     * Strips brackets, video definitions, lyrics tags and parentheticals.
     */
    private fun cleanGarbage(text: String): String {
        var result = text
        val garbagePatterns = listOf(
            Regex("\\(?Official\\s+(Music\\s+)?Video\\)?", RegexOption.IGNORE_CASE),
            Regex("\\(?Official\\s+(Music\\s+)?Audio\\)?", RegexOption.IGNORE_CASE),
            Regex("\\(?Official\\s+Visualizer\\)?", RegexOption.IGNORE_CASE),
            Regex("\\(?Official\\s+Lyric\\s+Video\\)?", RegexOption.IGNORE_CASE),
            Regex("\\[?Official\\s+(Music\\s+)?Video\\]?", RegexOption.IGNORE_CASE),
            Regex("\\[?Official\\s+(Music\\s+)?Audio\\]?", RegexOption.IGNORE_CASE),
            Regex("\\[?Official\\s+Visualizer\\]?", RegexOption.IGNORE_CASE),
            Regex("\\[?Official\\s+Lyric\\s+Video\\]?", RegexOption.IGNORE_CASE),
            Regex("\\(?Lyrics?\\)?", RegexOption.IGNORE_CASE),
            Regex("\\[?Lyrics?\\]?", RegexOption.IGNORE_CASE),
            Regex("\\(?Audio\\)?", RegexOption.IGNORE_CASE),
            Regex("\\[?Audio\\]?", RegexOption.IGNORE_CASE),
            Regex("\\[?HD\\]?", RegexOption.IGNORE_CASE),
            Regex("\\[?1080p\\]?", RegexOption.IGNORE_CASE),
            Regex("\\[?4K\\]?", RegexOption.IGNORE_CASE),
            Regex("\\(?HQ\\)?", RegexOption.IGNORE_CASE),
            Regex("\\[?HQ\\]?", RegexOption.IGNORE_CASE),
            Regex("\\(Official\\)", RegexOption.IGNORE_CASE),
            Regex("\\[Official\\]", RegexOption.IGNORE_CASE),
            Regex("\\(Remastered\\)", RegexOption.IGNORE_CASE),
            Regex("\\[Remastered\\]", RegexOption.IGNORE_CASE)
        )
        for (pattern in garbagePatterns) {
            result = result.replace(pattern, "")
        }
        
        // Strip other residual tags or noise symbols
        result = result
            .replace(Regex("\\(\\s*\\)"), "")
            .replace(Regex("\\[\\s*\\]"), "")
            .trim()
            
        // Strip trailing or leading dashes or pipes
        while (result.startsWith("-") || result.startsWith("|") || result.endsWith("-") || result.endsWith("|")) {
            result = result.removeSurrounding("-").removeSurrounding("|").trim()
        }
        return result
    }

    /**
     * Placeholder showing where a real Gemini API Call would go to perform
     * advanced Semantic Audio Cleaning & Metadata Enhancement.
     */
    fun cleanMetadataWithGemini(rawTitle: String): Pair<String, String> {
        // --- HOW THE REAL GEMINI CALL WOULD BE IMPLEMENTED ---
        // val model = Firebase.generativeModel("gemini-1.5-flash")
        // val prompt = "Extract the clean song title and main artist from this YouTube title: '$rawTitle'. Return in JSON format: {\"title\": \"...\", \"artist\": \"...\"}"
        // val response = model.generateContent(prompt)
        // val json = response.text
        // parseJson(json)
        
        // For now, return our robust local regex-based cleaner
        val cleaned = cleanMetadata(rawTitle)
        return Pair(cleaned.title, cleaned.artist)
    }
}
