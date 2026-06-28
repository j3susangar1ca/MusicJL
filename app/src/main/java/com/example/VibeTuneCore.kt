package com.example

import android.app.Application
import com.example.data.local.VibeTuneDatabase
import com.example.data.repository.DownloadRepositoryImpl
import com.example.domain.repository.DownloadRepository
import com.example.worker.NotificationHelper

/**
 * Clase principal de la Aplicación (Application Class).
 * Actúa como el punto de inicio global y contenedor de dependencias del proyecto.
 */
class VibeTuneCore : Application() {

    // Inicialización perezosa (Lazy) de la base de datos única de Room
    private val database by lazy { VibeTuneDatabase.getDatabase(this) }

    // Inicialización perezosa del repositorio para inyección manual limpia
    val downloadRepository: DownloadRepository by lazy { 
        DownloadRepositoryImpl(database.downloadDao()) 
    }

    override fun onCreate() {
        super.onCreate()

        // 1. Orquestación del Pipeline de Notificaciones Nativas
        // Al instanciar NotificationHelper aquí, nos aseguramos de crear 
        // el canal de descargas obligatorio de Android inmediatamente al arrancar la app.
        NotificationHelper(this)
    }
}