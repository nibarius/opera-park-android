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
                    "available" -> {
                        SpaceAvailableNotification(applicationContext, data).show()
                    }
                    else -> {
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
}