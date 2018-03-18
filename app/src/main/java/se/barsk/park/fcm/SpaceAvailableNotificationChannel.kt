package se.barsk.park.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import se.barsk.park.R


/**
 * Notification channel for notifications sent when the parking becomes available
 */
class SpaceAvailableNotificationChannel(val context: Context) {
    companion object {
        const val ID = "parking_available_channel"
    }

    private val name: String = context.getString(R.string.parking_available_notif_channel_name)
    private val description: String = context.getString(R.string.parking_available_notif_channel_description)

    @RequiresApi(Build.VERSION_CODES.O)
    fun create() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(ID, name, NotificationManager.IMPORTANCE_HIGH)
        channel.description = description
        channel.enableLights(true)
        channel.lightColor = context.getColor(R.color.colorPrimary)
        channel.enableVibration(true)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(channel)
    }
}