package se.barsk.park.fcm

import android.content.Context
import android.os.Build
import com.google.firebase.iid.FirebaseInstanceId


/**
 * Class to handle interactions with FCM and notifications.
 */
open class NotificationsManager {
    /**
     * The current FCM token. This method returns null if the token has not yet been generated.
     * This happens directly after first startup after install, but it will be ready very shortly
     * after startup so by the time the user have time to try to register to the wait list is
     * is available.
     */
    open val pushToken: String?
        get() = FirebaseInstanceId.getInstance().token
    //todo: fix deprecation https://firebase.google.com/docs/cloud-messaging/android/client
    // getting token is now an async call, need to handle things asynchronously

    /**
     * Creates all the notification channels used by the app.
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            SpaceAvailableNotificationChannel(context).create()
        }
    }
}