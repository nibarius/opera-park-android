package se.barsk.park.fcm

import android.app.Activity
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.app.NotificationChannel
import android.os.Build
import android.support.annotation.RequiresApi
import se.barsk.park.R


/**
 * Notification channel for notifications sent when the parking becomes free
 */
@RequiresApi(Build.VERSION_CODES.O)
class ParkingFreeNotificationChannel(val context: Context) {
    companion object {
        val ID = "parking_free_channel"
    }
    private val name: String = context.getString(R.string.parking_free_notif_channel_name)
    private val description: String = context.getString(R.string.parking_free_notif_channel_description)
    private val importance = NotificationManager.IMPORTANCE_HIGH

   fun create() {
       val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
       val channel = NotificationChannel(ID, name, importance)
       channel.description = description
       channel.enableLights(true)
       channel.lightColor = context.getColor(R.color.colorPrimary)
       channel.enableVibration(true)
       channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
       notificationManager.createNotificationChannel(channel)

   }
}