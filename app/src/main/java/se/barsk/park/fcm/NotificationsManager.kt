package se.barsk.park.fcm

import android.content.Context
import android.os.Build
import com.google.firebase.iid.FirebaseInstanceId


/**
 * Class to handle interactions with FCM and notifications.
 */
class NotificationsManager {
    /**
     * The current FCM token. This method returns null if the token has not yet been generated.
     * This happens directly after first startup after install, but it will be ready very shortly
     * after startup so by the time the user have time to try to register to the wait list is
     * is available.
     */
    val pushToken: String?
        get() = FirebaseInstanceId.getInstance().token

    /**
     * Creates all the notification channels used by the app.
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            SpaceAvailableNotificationChannel(context).create()
        }
    }
}