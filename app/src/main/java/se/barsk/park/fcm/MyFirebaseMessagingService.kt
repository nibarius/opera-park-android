package se.barsk.park.fcm

import android.util.Base64
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import se.barsk.park.R


/**
 * Service for handling incoming FCM messages.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            //todo: make server push notification type for future proofing
            val obj = JSONObject(remoteMessage.data)
            Log.e("JSON_OBJECT", obj.toString())
            val data = String(Base64.decode(remoteMessage.data["type_data"], Base64.DEFAULT)) //todo: don't crash on null
            val freeSpots = JSONObject(data)["free"]
            if (freeSpots == null) {
                //report event to firebase
                return
            }
            val title = applicationContext.getString(R.string.free_spots_notification_title, freeSpots)
            val body = applicationContext.getString(R.string.free_spots_notification_body)
            FcmManager(applicationContext).makeNotification(title, body)
        }
    }


    /* TODO: check for google play
Apps that rely on the Play Services SDK should always check the device for a compatible Google Play services APK before accessing Google Play services features. It is recommended to do this in two places: in the main activity's onCreate() method, and in its onResume() method. The check in onCreate() ensures that the app can't be used without a successful check. The check in onResume() ensures that if the user returns to the running app through some other means, such as through the back button, the check is still performed.

If the device doesn't have a compatible version of Google Play services, your app can call GoogleApiAvailability.makeGooglePlayServicesAvailable() to allow users to download Google Play services from the Play Store.

 */
}