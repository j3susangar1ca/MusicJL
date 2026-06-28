package com.example.data.remote

import com.example.domain.models.TrackInfo
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Interfaz que define los endpoints de comunicación con el pipeline de extracción.
 */
interface VibeTuneApiService {
    @GET("rest/v1/audio-extractor")
    suspend fun getConvertedTrackInfo(
        @Query("video_id") videoId: String,
        @Query("apikey") apiKey: String
    ): TrackInfo
}

/**
 * Cliente Remoto Singleton configurado bajo la infraestructura robusta declarada en Gradle.
 */
object SupabaseClient {

    // Nota técnica: En producción estos valores se inyectan automáticamente desde tu plugin de Secrets (.env)
    private const val BASE_URL = "https://placeholder-supabase-url.supabase.co/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS) // Tiempos amplios para dar margen al handshake asíncrono
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create()) // Conversión directa a tus data classes con KSP
        .build()

    val apiService: VibeTuneApiService by lazy {
        retrofit.create(VibeTuneApiService::class.java)
    }
}