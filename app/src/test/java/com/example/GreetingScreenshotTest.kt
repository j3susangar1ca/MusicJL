package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.components.CapturingStateView
import com.example.ui.components.MetadataReadyView
import com.github.takahirom.roborazzi.captureRoborazzi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * Suite de pruebas de regresión visual para la interfaz premium de VibeTune.
 * Captura y valida el estado exacto de los componentes frosted glass.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE) // Obligatorio para renderizado nativo de fuentes y sombras con Roborazzi
class GreetingScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun capture_capturingStateView_premiumGlow() {
        composeTestRule.setContent {
            // Evaluamos tu componente con animación y estados frosted
            CapturingStateView()
        }

        // Roborazzi congela el frame y genera el PNG de prueba en la carpeta de assets
        composeTestRule.onRoot().captureRoborazzi("src/test/screenshots/capturing_state_premium.png")
    }

    @Test
    fun capture_metadataReadyView_horizontalFrostedCard() {
        composeTestRule.setContent {
            MetadataReadyView(
                title = "Die With A Smile",
                artist = "Lady Gaga, Bruno Mars",
                albumArtIndex = 0,
                onDownloadStart = {},
                onCancel = {}
            )
        }

        // Valida que el badge "METADATOS LIMOPOS" y la tarjeta mantengan el diseño top
        composeTestRule.onRoot().captureRoborazzi("src/test/screenshots/metadata_ready_premium.png")
    }
}
