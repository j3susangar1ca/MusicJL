package com.example.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

/**
 * Gestor de notificaciones nativas encargado de enviar feedback visual
 * sobre el progreso de las descargas en segundo plano.
 */
class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "vibetune_downloads_channel"
        private const val CHANNEL_NAME = "Descargas VibeTune"
        private const val CHANNEL_DESC = "Muestra el progreso de conversión de audio de YouTube"
        private const val BASE_NOTIFICATION_ID = 2026
    }

    init {
        createNotificationChannel()
    }

    /**
     * Registra el canal de comunicación obligatorio para Android Oreo (8.0) en adelante.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW // Evita interrupciones sonoras molestas por cada paso de progreso
            ).apply {
                description = CHANNEL_DESC
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Construye y despliega la notificación de progreso real (Rich Notification).
     * Muestra la barra de progreso en bytes, título limpio, artista y botón para cancelar.
     */
    fun showProgressNotification(
        videoId: String,
        title: String,
        artist: String,
        progress: Int
    ) {
        val notificationId = abs(videoId.hashCode())

        // Intent para abrir la app si el usuario toca la notificación
        val contentIntent = Intent(context, MainActivity::class.java).let { intent ->
            PendingIntent.getActivity(
                context, 
                notificationId, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        // --- Botón de Acción Especial: Cancelar Tarea ---
        // Envía un broadcast ficticio al sistema para abortar el WorkManager de esta descarga
        val cancelIntent = Intent("com.example.ACTION_CANCEL_DOWNLOAD").apply {
            putExtra("VIDEO_ID", videoId)
        }
        val cancelPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download) // Ícono nativo de descarga en curso
            .setContentTitle(title)
            .setContentText("Procesando pista de $artist...")
            .setSubText("VibeTune Engine")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true) // Impide que el usuario la descarte deslizando el dedo
            .setContentIntent(contentIntent)
            .setProgress(100, progress, false) // Actualiza de forma fluida (0-100%)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel, 
                "Cancelar", 
                cancelPendingIntent
            )

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    /**
     * Reemplaza la notificación de progreso por una de éxito con estilo multimedia (MediaStyle).
     */
    fun showCompletedNotification(videoId: String, title: String, artist: String) {
        val notificationId = abs(videoId.hashCode())

        // Limpiamos la notificación de progreso bloqueada anterior
        notificationManager.cancel(notificationId)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done) // Ícono de descarga completada
            .setContentTitle("¡Descarga Exitosa!")
            .setContentText("$title — $artist")
            .setSubText("Biblioteca Actualizada")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true) // Desaparece automáticamente cuando el usuario la toca
            .build()

        notificationManager.notify(notificationId + 1, notification)
    }
}
}
