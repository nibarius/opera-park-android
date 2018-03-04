package se.barsk.park.fcm

import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONException
import org.json.JSONObject
import se.barsk.park.ParkApp
import se.barsk.park.R


/**
 * Service for handling incoming FCM messages.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            Log.e("JSON_OBJECT", JSONObject(remoteMessage.data).toString())  //todo: remove logging
            val type = remoteMessage.data["type"]
            val rawData = remoteMessage.data["type_data"]
            if (type == null || rawData == null) {
                //todo: report to firebase
                return
            }
            try {
                val data = JSONObject(String(Base64.decode(rawData, Base64.DEFAULT)))
                when (type) {
                    "not_full" -> {  //todo: call it available instead
                        spaceAvailableNotification(data)
                    }
                    else -> {
                        //todo: report unknown notification to firebase
                    }
                }
            } catch (e: JSONException) {
                //todo: report to firebase. json decoding failed
            } catch (e: IllegalArgumentException) {
                //Todo: report to firebase. base64 decoding failed
            }
        }
    }

    private fun spaceAvailableNotification(data: JSONObject) {
        val freeSpots = data["free"]
        if (freeSpots == null || freeSpots == "0") {
            //todo: report event to firebase
            return
        }
        val title = applicationContext.getString(R.string.free_spots_notification_title, freeSpots)
        val body = applicationContext.getString(R.string.free_spots_notification_body)
        FcmManager(applicationContext).makeNotification(title, body)
        if (ParkApp.isRunning()) {
            // Update local wait list state (on the UI thread) if the app is running
            Handler(Looper.getMainLooper()).post { ParkApp.theUser.isOnWaitList = false }
        }
    }


    /* TODO: check for google play
Apps that rely on the Play Services SDK should always check the device for a compatible Google Play services APK before accessing Google Play services features. It is recommended to do this in two places: in the main activity's onCreate() method, and in its onResume() method. The check in onCreate() ensures that the app can't be used without a successful check. The check in onResume() ensures that if the user returns to the running app through some other means, such as through the back button, the check is still performed.

If the device doesn't have a compatible version of Google Play services, your app can call GoogleApiAvailability.makeGooglePlayServicesAvailable() to allow users to download Google Play services from the Play Store.

 */
}