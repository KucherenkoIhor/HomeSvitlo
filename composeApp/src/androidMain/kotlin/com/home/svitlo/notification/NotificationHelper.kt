package com.home.svitlo.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.home.svitlo.MainActivity
import com.home.svitlo.R
import com.home.svitlo.domain.model.InverterStatus

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "inverter_status_channel"
        const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Статус інвертора"
            val descriptionText = "Сповіщення про зміну статусу електроенергії"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showStatusChangeNotification(newStatus: InverterStatus, batteryCharge: Double) {
        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val (title, message, emoji) = when (newStatus) {
            InverterStatus.NORMAL -> Triple(
                "☀️ Світло є!",
                "Електроенергія відновлена. Батарея: ${batteryCharge.toInt()}%",
                "☀️"
            )
            InverterStatus.OFF_GRID -> Triple(
                "🔌 Світла немає!",
                "Працює автономний режим. Батарея: ${batteryCharge.toInt()}%",
                "🔌"
            )
            else -> Triple(
                "🔄 Статус змінився",
                "${newStatus.description}. Батарея: ${batteryCharge.toInt()}%",
                "🔄"
            )
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}

