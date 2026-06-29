package com.example.data.remote

import com.example.BuildConfig
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
    
    const val API_KEY = BuildConfig.SUPABASE_API_KEY

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
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