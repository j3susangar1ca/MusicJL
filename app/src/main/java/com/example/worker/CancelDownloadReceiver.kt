package com.example.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.WorkManager

class CancelDownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.example.ACTION_CANCEL_DOWNLOAD") {
            val videoId = intent.getStringExtra("VIDEO_ID") ?: return
            Log.d("CancelDownloadReceiver", "Cancelling download for videoId: $videoId")
            WorkManager.getInstance(context).cancelAllWorkByTag(videoId)
        }
    }
}
