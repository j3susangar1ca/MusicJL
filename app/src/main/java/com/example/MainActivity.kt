package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.MainDashboardScreen
import com.example.ui.screens.ShareSheetOverlay
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check if activity was launched via YouTube "Share" sheet
        val intentAction = intent?.action
        val intentType = intent?.type
        val isShareSheetFlow = intentAction == Intent.ACTION_SEND && intentType == "text/plain"
        val sharedText = intent?.getStringExtra(Intent.EXTRA_TEXT) ?: ""

        setContent {
            MyApplicationTheme {
                val viewModel: VibeTuneViewModel = viewModel()
                val context = LocalContext.current

                // Trigger processing automatically if shared
                LaunchedEffect(intentAction) {
                    viewModel.refreshHistory(context)
                    if (isShareSheetFlow && sharedText.isNotEmpty()) {
                        viewModel.processUrl(context, sharedText)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (isShareSheetFlow) Color.Transparent else MaterialTheme.colorScheme.background
                ) {
                    if (isShareSheetFlow) {
                        // Translucent Share Sheet Flow (Floating over YouTube)
                        ShareSheetOverlay(
                            viewModel = viewModel,
                            onDismiss = { finish() }
                        )
                    } else {
                        // Fully immersive dark dashboard for manual conversions and history
                        MainDashboardScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}
