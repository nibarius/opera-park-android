package se.barsk.park.fcm

import android.content.Context
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import se.barsk.park.error.ErrorHandler
import se.barsk.park.Injection
import se.barsk.park.ParkApp
import se.barsk.park.R

/**
 * A space available notification that let's the user know that there is now free space
 * in the garage.
 *
 * Note: The app may or may not be running when this notification is shown.
 */
class SpaceAvailableNotification(context: Context, data: JSONObject) : Notification(context, data) {

    override val id = SpaceAvailableNotificationChannel.ID
    override val timeout: Long = 3 * 60 * 60 * 1000 // 3 hours
    private val freeSpots = data["free"]
    private val valid: Boolean = when (freeSpots) {
        null -> {
            ErrorHandler.raiseException("Invalid space available notification, free parameter missing")
            false
        }
        "0" -> {
            ErrorHandler.raiseException("Invalid space available notification, 0 spaces free")
            false
        }
        else ->
            true
    }

    override fun show() {
        if (!valid) {
            return
        }
        val storageManager = Injection.provideStorageManager(context)
        if (!storageManager.onWaitList()) {
            // If we're not on the wait list but still get a notification discard it.
            // This may happen if the user signs out without internet connection while
            // on the wait list. If this happens we just ignore the notification.
            return
        }

        val title = context.getString(R.string.free_spots_notification_title, freeSpots)
        val body = context.getString(R.string.free_spots_notification_body)
        makeNotification(title, body)
        if (ParkApp.isRunning()) {
            // Update local wait list state (on the UI thread) if the app is running
            Handler(Looper.getMainLooper()).post { ParkApp.theUser.isOnWaitList = false }
        } else {
            // If the app is not running, update the persistent value instead so we have
            // the correct state when the app is started the next time.
            storageManager.setOnWaitList(false)
        }
    }
}