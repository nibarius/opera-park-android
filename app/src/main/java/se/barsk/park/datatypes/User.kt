package se.barsk.park.datatypes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import se.barsk.park.ParkApp
import se.barsk.park.SignInHandler
import se.barsk.park.mainui.ErrorMessage
import se.barsk.park.network.Result
import kotlin.properties.Delegates

/**
 * The representation of the user of the app.
 */
class User(context: Context) {
    interface ChangeListener {
        fun onSignInStatusChanged()
        /**
         * Called whenever the sign in process fails in an erroneous way.
         * It is not called when it fails due to user canceling the sign in dialog.
         */
        fun onSignInFailed(statusCode: Int)
        fun onWaitListStatusChanged()
    }

    private val signInHandler = SignInHandler(context, SignInListener())
    private var listeners: MutableList<ChangeListener> = mutableListOf()

    val accountName: String
        get() = signInHandler.getAccountName()

    var isSignedIn: Boolean by Delegates.observable(signInHandler.isSignedIn()) { _, _, _ ->
        listeners.forEach { it.onSignInStatusChanged() }
    }
    var isOnWaitList: Boolean by Delegates.observable(false) { _, _, _ ->
        listeners.forEach { it.onWaitListStatusChanged() }
    }

    fun addListener(listener: ChangeListener) = listeners.add(listener)
    fun removeListener(listener: ChangeListener) = listeners.remove(listener)

    fun addToWaitList(activity: Activity, containerView: View? = null) {
        signInHandler.doWithFreshToken(activity) { freshToken ->
            WaitList(activity.applicationContext, containerView).add(freshToken)
        }
    }

    fun removeFromWaitList(activity: Activity, containerView: View? = null) {
        signInHandler.doWithFreshToken(activity) { freshToken ->
            WaitList(activity.applicationContext, containerView).remove(freshToken)
        }
    }

    /**
     * Tries to sign in the user silently asynchronously. On completion the
     * onSuccess function is called and if there is any change in sign in status
     * the registered listener is notified.
     *
     * @param activity If onStop is called on the given activity the login will be aborted
     * @param onSuccess function to run once the login has finished successfully
     */
    fun silentSignIn(activity: Activity, onSuccess: (() -> Unit)? = null) {
        signInHandler.silentSignIn(activity, onSuccess)
    }

    /**
     * Signs in the user to the app showing the necessary sign in UI. On completion the
     * onSuccess function is called and if there is any change in sign in status
     * the registered listener is notified.
     *
     * @param activity If onStop is called on the given activity the login will be aborted
     * @param onSuccess function to run once the login has finished successfully
     */
    fun signIn(activity: Activity, onSuccess: (() -> Unit)? = null) {
        signInHandler.signIn(activity, onSuccess)
    }

    fun signOut(activity: Activity) {
        if (isOnWaitList) {
            removeFromWaitList(activity, null)
            //todo: ignore error and just don't show notifications when signed out. or something better?
        }
        signInHandler.signOut()
    }

    /**
     * Function that should be called by the ParkActivity when the sign in result is ready.
     */
    fun onSignInResult(data: Intent) {
        signInHandler.onSignInResult(data)
    }

    inner class SignInListener : SignInHandler.StatusChangedListener {
        override fun onSignedIn() {
            isSignedIn = true
        }

        override fun onSignedOut() {
            isSignedIn = false
        }

        override fun onSignInFailed(statusCode: Int) {
            listeners.forEach { it.onSignInFailed(statusCode) }
        }
    }

    /**
     * Class for handling the wait list that users can sign up to in order
     * to get notifications when the garage becomes available.
     */
    private inner class WaitList(private val appContext: Context, private val containerView: View? = null) {

        fun add(token: String) {
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
                    ErrorMessage(containerView).show(result.message) //todo: possible to use listener or similar to get rid of container view?
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