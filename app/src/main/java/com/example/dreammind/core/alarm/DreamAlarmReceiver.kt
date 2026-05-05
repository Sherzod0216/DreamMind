package com.example.dreammind.core.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.dreammind.R

class DreamAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ensureChannel(context)
        val alarmTime = intent.getStringExtra(EXTRA_ALARM_TIME).orEmpty()
        val sound = intent.getStringExtra(EXTRA_ALARM_SOUND) ?: "DreamMind"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("DreamMind Smart Alarm")
            .setContentText("Wake gently with $sound. Alarm time: ${alarmTime.ifBlank { "now" }}.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "DreamMind alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Smart alarm notifications from DreamMind"
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val EXTRA_ALARM_TIME = "dreammind_alarm_time"
        const val EXTRA_ALARM_SOUND = "dreammind_alarm_sound"
        private const val CHANNEL_ID = "dreammind_alarm_channel"
        private const val NOTIFICATION_ID = 4208
    }
}
