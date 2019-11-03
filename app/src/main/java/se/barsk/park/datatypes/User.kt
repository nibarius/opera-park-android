package se.barsk.park.datatypes

import android.app.Activity
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.*
import se.barsk.park.Injection
import se.barsk.park.ParkApp
import se.barsk.park.R
import se.barsk.park.SignInHandler
import se.barsk.park.error.ErrorHandler
import se.barsk.park.error.FailedToGetFcmTokenException
import se.barsk.park.error.FailedToSignInException
import se.barsk.park.fcm.NotificationsManager
import se.barsk.park.network.Result
import kotlin.properties.Delegates

/**
 * The representation of the user of the app.
 */
open class User(context: Context) {
    interface ChangeListener {
        fun onSignInStatusChanged()
        /**
         * Called whenever the sign in process fails in an erroneous way.
         * It is not called when it fails due to user canceling the sign in dialog.
         */
        fun onSignInFailed(statusCode: Int)

        fun onWaitListStatusChanged()
        fun onWaitListFailed(message: String)
    }

    private val signInHandler = Injection.provideSignInHandler(context, SignInListener())
    private var listeners: MutableList<ChangeListener> = mutableListOf()

    val accountName: String
        get() = signInHandler.getAccountName()

    var isSignedIn: Boolean by Delegates.observable(signInHandler.isSignedIn()) { _, _, _ ->
        listeners.forEach { it.onSignInStatusChanged() }
    }

    // Initialize from persistent storage and persist changes to have correct state without
    // talking to the backend.
    var isOnWaitList: Boolean by Delegates.observable(ParkApp.storageManager.onWaitList()) { _, _, newValue ->
        ParkApp.storageManager.setOnWaitList(newValue)
        listeners.forEach { it.onWaitListStatusChanged() }
    }

    fun addListener(listener: ChangeListener) = listeners.add(listener)
    fun removeListener(listener: ChangeListener) = listeners.remove(listener)

    /**
     * Asynchronously adds the user to the wait list by making a request to the backend. Fetches
     * an up to date version of both the push token and user id token before doing the request.
     */
    open fun addToWaitList(activity: Activity) {

        val context = activity.applicationContext
        GlobalScope.launch(Dispatchers.Main) {
            supervisorScope {
                try {
                    val pushToken = async(Dispatchers.IO) { NotificationsManager().getToken() }
                    val idToken = async(Dispatchers.IO) { signInHandler.getToken(activity) }
                    WaitList(context).add(idToken.await(), pushToken.await())
                } catch (e: Exception) {
                    val toFirebase = "addToWaitList failed: ${e.message}"
                    val toUser = when (e) {
                        is FailedToGetFcmTokenException ->
                            context.getString(R.string.can_not_get_fcm_token)
                        is FailedToSignInException ->
                            context.getString(R.string.wait_list_register_failed_sign_in_problem)
                        else -> context.getString(R.string.wait_list_register_failed_sign_in_problem)
                    }
                    reportError(toUser, toFirebase)
                }
            }
        }
    }

    open fun removeFromWaitList(activity: Activity) {
        val context = activity.applicationContext
        GlobalScope.launch(Dispatchers.Main) {
            supervisorScope {
                try {
                    val idToken = async(Dispatchers.IO) {
                        signInHandler.getToken(activity)
                    }
                    WaitList(context).remove(idToken.await())
                } catch (e: Exception) {
                    val toFirebase = "removeFromWaitList failed: ${e.message}"
                    val toUser = context.getString(R.string.wait_list_unregister_failed_sign_in_problem)
                    reportError(toUser, toFirebase)
                }
            }
        }
    }

    private fun reportError(toUser: String, toFirebase: String) {
        listeners.forEach {
            it.onWaitListFailed(toUser)
        }
        ErrorHandler.raiseException(toFirebase)
    }

    /**
     * Tries to sign in the user silently asynchronously. If there are no errors
     * there will be no notifications, the user will just not be signed in and
     * the UI will offer a sign in option when the user opens the menu.
     *
     * Only tries to sign in if a new enough version of Google play services is installed.
     *
     * @param activity If onStop is called on the given activity the login will be aborted
     */
    fun silentSignIn(activity: Activity) = signInHandler.silentSignIn(activity)

    /**
     * Signs in the user to the app showing the necessary sign in UI. On completion the
     * onSuccess function is called and if there is any change in sign in status
     * the registered listener is notified.
     *
     * If Google play services is not installed an error dialog will be shown instead
     * of signing in.
     *
     * @param activity If onStop is called on the given activity the login will be aborted
     * @param onSuccess function to run once the login has finished successfully
     */
    fun signIn(activity: Activity, onSuccess: (() -> Unit)? = null) {
        signInHandler.signIn(activity, onSuccess)
    }

    fun signOut(activity: Activity) {
        val token = signInHandler.token
        if (isOnWaitList && token != null) {
            // If on the wait list, do a one shot attempt at removing from the wait list
            // with the previously cached token. Getting an up to date token would cause a
            // silent log in which there is no point in doing when trying to log out.
            // It's not critical if the un-registering fails since we will ignore
            // push notifications received when logged out.
            WaitList(activity.applicationContext).remove(token)

            // Set local state to false regardless if the request to the server succeeded or not,
            // when the user is logged out the user is not on the wait list.
            isOnWaitList = false
        }
        signInHandler.signOut()
    }

    /**
     * Function that should be called by the ParkActivity when the sign in result is ready.
     */
    fun onSignInResult(data: Intent?) {
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
    protected inner class WaitList(private val appContext: Context) {

        fun add(idToken: String, pushToken: String) {
            ParkApp.networkManager.addToWaitList(appContext, idToken, pushToken, this::onWaitListResultReady)
        }

        fun remove(idToken: String) {
            ParkApp.networkManager.removeFromWaitList(appContext, idToken, this::onWaitListResultReady)
        }

        private fun onWaitListResultReady(result: Result) = when (result) {
            is Result.AddedToWaitList -> {
                this@User.isOnWaitList = true
            }
            is Result.RemovedFromWaitList -> {
                this@User.isOnWaitList = false
            }
            is Result.Fail -> {
                listeners.forEach { it.onWaitListFailed(result.message) }
            }
            else -> {
                throw RuntimeException("Unexpected result type returned by wait list")
            }
        }
    }
}