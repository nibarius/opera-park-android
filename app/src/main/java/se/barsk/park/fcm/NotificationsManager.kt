package se.barsk.park.fcm

import android.content.Context
import android.os.Build
import com.google.firebase.messaging.FirebaseMessaging
import se.barsk.park.error.FailedToGetFcmTokenException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


/**
 * Class to handle interactions with FCM and notifications.
 */
class NotificationsManager {
    /**
     * Creates all the notification channels used by the app.
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            SpaceAvailableNotificationChannel(context).create()
        }
    }

    /**
     * Get the FCM token or throw an exception if it's not possible to get it.
     */
    @Suppress("unused") // Used in prod flavor, but not in mock
    suspend fun getToken(): String = suspendCoroutine { continuation ->
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                continuation.resumeWithException(FailedToGetFcmTokenException(task.exception?.message
                        ?: "Unknown error when getting FCM token"))
                return@addOnCompleteListener
            }
            val token = task.result
            continuation.resume(token)
        }
    }
}