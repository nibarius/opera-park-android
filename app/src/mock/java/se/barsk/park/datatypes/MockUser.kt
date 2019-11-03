package se.barsk.park.datatypes

import android.app.Activity
import android.content.Context

// Robolectric doesn't work well with coroutines since it need everything to run
// on the main thread. We're not getting real tokens during test anyway so
// just skip asynchronous fetching of tokens and do the wait list operations
// immediately instead when mocking.
class MockUser(context: Context): User(context) {
    override fun addToWaitList(activity: Activity) {
        WaitList(activity.applicationContext).add("idToken", "pushToken")
    }

    override fun removeFromWaitList(activity: Activity) {
        WaitList(activity.applicationContext).remove("idToken")
    }
}