package se.barsk.park.fcm

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceIdService
import com.google.firebase.iid.FirebaseInstanceId



/**
 * Service for handling the creation, rotation, and updating of registration tokens.
 * This is required for sending to specific devices or for creating device groups.
 */
class MyFirebaseInstanceIDService : FirebaseInstanceIdService() {

    // Called whenever the token changes. No need for it since we send the token to
    // the server whenever registering for pushes.
    override fun onTokenRefresh() {
        // Get updated InstanceID token.
        //currentToken = FirebaseInstanceId.getInstance().token
        //Log.d("barsk", "Refreshed token: " + currentToken!!)
    }
}