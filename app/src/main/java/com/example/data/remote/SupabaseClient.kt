package com.example.data.remote

import com.example.BuildConfig // Generado automáticamente por el plugin de Secrets de tu Gradle
import com.example.domain.models.TrackInfo
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface VibeTuneApiService {
    // Apunta al endpoint de tu tabla o Edge Function respetando la seguridad RLS
    @GET("rest/v1/audio-extractor")
    suspend fun getConvertedTrackInfo(
        @Query("video_id") videoId: String,
        @Header("apikey") apiKey: String = BuildConfig.SUPABASE_ANON_KEY,
        @Header("Authorization") token: String = "Bearer ${BuildConfig.SUPABASE_ANON_KEY}"
    ): List<TrackInfo> // Supabase REST devuelve las consultas dentro de un arreglo JSON por defecto
}

object SupabaseClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Registra todo el tráfico en el Logcat para depurar
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.SUPABASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create()) // Moshi Codegen vía KSP se encarga del parseo sin reflexión
        .build()

    val apiService: VibeTuneApiService by lazy {
        retrofit.create(VibeTuneApiService::class.java)
    }
}
