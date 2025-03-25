package com.eysamarin.squadplay

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Without cloud functions or any BE we no need to store newToken.
 */
@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class SquadPlayMessagingService: FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "From: ${remoteMessage.from}")
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Message data payload: ${remoteMessage.data}")
        }

        remoteMessage.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
            createNotification(title = it.title, body = it.body)
        }
    }

    private fun createNotification(title: String?, body: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("FCM", "Notification permission is not granted")
                return
            }
        }

        createNotificationChannel()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            /* context = */ this,
            /* requestCode = */ 0,
            /* intent = */ intent,
            /* flags = */ PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title ?: "New Event")
            .setContentText(body ?: "You have a new event")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun createNotificationChannel() {
        val name = "Event Notifications"
        val descriptionText = "Notifications about new events"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
            .apply { description = descriptionText }

        val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE)
                as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "event_notifications"
        private const val NOTIFICATION_ID = 1
    }

}