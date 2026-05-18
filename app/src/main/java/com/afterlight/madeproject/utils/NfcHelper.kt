package com.afterlight.madeproject.utils

import android.app.Activity
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter

object NfcHelper {
    fun enableNfcSharing(activity: Activity, eventId: String) {
        // Android Beam (setNdefPushMessage) was completely removed in API 30+.
        // The modern replacement for Tap-to-Share is Android Nearby Share,
        // which is triggered via the generic Share intent (ACTION_SEND).
    }

    fun disableNfcSharing(activity: Activity) {
        // No-op for modern Android.
    }

    fun shareEventGeneric(activity: Activity, eventId: String, title: String) {
        val url = "https://paperlike.app/events/$eventId"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "Check out this event: $title\n$url")
        }
        activity.startActivity(Intent.createChooser(intent, "Share Event"))
    }
}
