package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.screens.MainDashboardScreen
import com.example.ui.screens.ShareSheetOverlay
import com.example.ui.theme.VibeTuneTheme

class MainActivity : ComponentActivity() {

    // Inicialización nativa del ViewModel integrado con el ciclo de vida de Android
    private val viewModel: VibeTuneViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Evaluar si la actividad fue despertada a través del menú "Compartir" de YouTube
        handleIncomingIntent(intent)

        setContent {
            VibeTuneTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 1. Pantalla Base: El panel de control e historial de descargas
                    MainDashboardScreen(viewModel = viewModel)

                    // 2. Capa Superior Flotante: Solo se dibuja si el estado no es 'Idle'
                    // Reacciona al menú Compartir de YouTube y flota sobre la interfaz anterior
                    ShareSheetOverlay(
                        viewModel = viewModel,
                        onDismiss = {
                            // Al cerrar el diálogo, si entramos por el menú compartir, finalizamos la actividad
                            if (intent?.action == Intent.ACTION_SEND) {
                                finish()
                            }
                        }
                    )
                }
            }
        }
    }

    /**
     * Intercepta cambios de Intent si la actividad ya está abierta en segundo plano
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    /**
     * Extrae el texto o enlace compartido desde aplicaciones externas (YouTube / YouTube Music)
     */
    private fun handleIncomingIntent(intent: Intent?) {
        if (intent != null && intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrEmpty()) {
                // Pasamos el texto al procesador lógico de tu ViewModel
                viewModel.processUrl(this, sharedText)
            }
        }
    }
}