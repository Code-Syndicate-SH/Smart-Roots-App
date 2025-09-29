package com.example.smarthydro.aidailyanomaly.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.smarthydro.aidailyanomaly.domain.model.DailyReport

object Notifier {
    private const val CHANNEL_ID = "daily_anomaly_channel"

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                val ch = NotificationChannel(
                    CHANNEL_ID,
                    "Daily Anomaly Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                nm.createNotificationChannel(ch)
            }
        }
    }

    // NEW: label + mac to tag which tent the report is for
    fun showDailyResult(context: Context, report: DailyReport, label: String, mac: String) {
        ensureChannel(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val prefix = "[$label]" // e.g., [Vegetable Tent]
        val title = if (report.status == "HEALTHY")
            "$prefix System healthy"
        else
            "$prefix Anomalies detected"

        val text = report.summary

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // system icon
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .build()

        // NEW: stable per-device notification ID so both can show separately
        val notificationId = (label + mac).hashCode()
        nm.notify(notificationId, notif)
    }
}
