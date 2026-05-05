package com.example.dreammind.core.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.dreammind.data.AlarmState
import java.util.Calendar

class AlarmScheduler(
    private val context: Context
) {
    fun schedule(alarm: AlarmState) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            Intent(context, DreamAlarmReceiver::class.java).apply {
                putExtra(DreamAlarmReceiver.EXTRA_ALARM_TIME, alarm.alarmTime)
                putExtra(DreamAlarmReceiver.EXTRA_ALARM_SOUND, alarm.soundOptions.firstOrNull { it.selected }?.title)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            alarm.nextTriggerMillis(),
            pendingIntent
        )
    }

    private fun AlarmState.nextTriggerMillis(): Long {
        val parts = alarmTime.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 7
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }.timeInMillis
    }

    private companion object {
        const val REQUEST_CODE = 4207
    }
}
