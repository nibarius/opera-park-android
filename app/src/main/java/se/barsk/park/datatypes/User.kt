package se.barsk.park.datatypes

import android.content.Context
import android.view.View
import se.barsk.park.ParkApp
import se.barsk.park.mainui.ErrorMessage
import se.barsk.park.network.Result
import kotlin.properties.Delegates

/**
 * The representation of the user of the app.
 */
class User(signedIn: Boolean = false, isOnWaitList: Boolean = false) {
    var signedIn: Boolean by Delegates.observable(signedIn) { _, _, _ -> notifyListeners() }
    var isOnWaitList: Boolean by Delegates.observable(isOnWaitList) { _, _, _ -> notifyListeners() }

    fun addListener(listener: UserChangedListener) = listeners.add(listener)

    fun addToWaitList(appContext: Context, containerView: View? = null, token: String) {
        WaitList(appContext, containerView).add(token)
    }

    fun removeFromWaitList(appContext: Context, containerView: View? = null, token: String) {
        WaitList(appContext, containerView).remove(token)
    }

    private var listeners: MutableList<UserChangedListener> = mutableListOf()
    private fun notifyListeners() {
        for (listener in listeners) {
            listener.onUserChanged()
        }
    }

    /**
     * Class for handling the wait list that users can sign up to in order
     * to get notifications when the garage becomes available.
     */
    private inner class WaitList(private val appContext: Context, private val containerView: View? = null) {

        fun add(token: String) {
            //Todo: if calling this too fast on startup there might not be a token and it will fail
            // if there is no token the call should be delayed until there is one, or time out
            // with an error after a while
            ParkApp.networkManager.addToWaitList(appContext, token, this::onWaitListResultReady)
        }

        fun remove(token: String) {
            ParkApp.networkManager.removeFromWaitList(appContext, token, this::onWaitListResultReady)
        }

        private fun onWaitListResultReady(result: Result) = when (result) {
            is Result.AddedToWaitList -> {
                this@User.isOnWaitList = true
            }
            is Result.RemovedFromWaitList -> {
                this@User.isOnWaitList = false
            }
            is Result.Fail -> {
                if (containerView != null) {
                    ErrorMessage(containerView).show(result.message)
                } else {
                    // No view to show the error message, just ignore it.
                }
            }
            else -> {
                throw RuntimeException("Unexpected result type returned by wait list")
            }
        }
    }
}