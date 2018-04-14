package se.barsk.park.fcm

import android.util.Base64
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONException
import org.json.JSONObject
import se.barsk.park.ErrorHandler


/**
 * Service for handling incoming FCM messages.
 */
class ParkFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            Log.e("JSON_OBJECT", JSONObject(remoteMessage.data).toString())  //todo: remove logging
            val type = remoteMessage.data["type"]
            val rawData = remoteMessage.data["type_data"]
            if (type == null || rawData == null) {
                var msg = "Invalid FCM push"
                if (type == null) {
                    msg += ", type is null"
                }
                if (rawData == null) {
                    msg += ", type_data is null"
                }
                ErrorHandler.raiseException(msg)
                return
            }
            try {
                val data = JSONObject(String(Base64.decode(rawData, Base64.DEFAULT)))
                when (type) {
                    "not_full" -> {  //todo: call it available instead
                        SpaceAvailableNotification(applicationContext, data).show()
                    }
                    else -> { //Todo, does the server have everything it needs to not push messages not supported by the client?
                        ErrorHandler.raiseException("Invalid FCM push, unknown type: $type")
                    }
                }
            } catch (e: JSONException) {
                ErrorHandler.raiseException("Invalid FCM push, json decoding failed", e)
            } catch (e: IllegalArgumentException) {
                ErrorHandler.raiseException("Invalid FCM push, base64 decoding failed", e)
            }
        }
    }

    /* TODO: check for google play
Apps that rely on the Play Services SDK should always check the device for a compatible Google Play services APK before accessing Google Play services features. It is recommended to do this in two places: in the main activity's onCreate() method, and in its onResume() method. The check in onCreate() ensures that the app can't be used without a successful check. The check in onResume() ensures that if the user returns to the running app through some other means, such as through the back button, the check is still performed.

If the device doesn't have a compatible version of Google Play services, your app can call GoogleApiAvailability.makeGooglePlayServicesAvailable() to allow users to download Google Play services from the Play Store.

 */
}