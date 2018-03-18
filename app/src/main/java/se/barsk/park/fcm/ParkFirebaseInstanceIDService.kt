package se.barsk.park.fcm

import com.google.firebase.iid.FirebaseInstanceIdService


/**
 * Service for handling the creation, rotation, and updating of registration tokens.
 * This is required for sending to specific devices or for creating device groups.
 */
class ParkFirebaseInstanceIDService : FirebaseInstanceIdService() {

    // Called whenever the token changes. No need for it since we send the token to
    // the server whenever registering for pushes. But keep the class for easy access
    // if needed in the future, or when debugging.
    override fun onTokenRefresh() {}
}