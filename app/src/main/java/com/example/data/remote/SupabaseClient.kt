package com.example.data.remote

import com.example.domain.models.TrackInfo
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Interfaz que define los endpoints de comunicación con el pipeline de extracción.
 */
interface VibeTuneApiService {
    @Headers("Accept: application/vnd.pgrst.object+json")
    @GET("rest/v1/audio-extractor")
    suspend fun getConvertedTrackInfo(
        @Query("video_id") videoIdFilter: String,
        @Query("apikey") apiKey: String
    ): TrackInfo
}

/**
 * Cliente Remoto Singleton configurado con la instancia de Supabase real.
 */
object SupabaseClient {

    private const val BASE_URL = "https://zsisrdvqkdcmolqshius.supabase.co/"
    
    const val API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InpzaXNyZHZxa2RjbW9scXNoaXVzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODI2NjI0MzIsImV4cCI6MjA5ODIzODQzMn0.hH1SCr9Ymk201AGe49aTpSaGX4Lx2dK9JwlwzkLiniE"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val apiService: VibeTuneApiService by lazy {
        retrofit.create(VibeTuneApiService::class.java)
    }
}