package se.barsk.park

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import se.barsk.park.analytics.UserProperties

// Google documentation for sign-in: https://developers.google.com/identity/sign-in/android/start
// Also useful: https://stackoverflow.com/questions/38737021/how-to-refresh-id-token-after-it-expires-when-integrating-google-sign-in-on-andr

/**
 * Handler for signing in to the app.
 */
open class SignInHandler(context: Context, protected val listener: StatusChangedListener) {
    interface StatusChangedListener {
        fun onSignedIn()
        fun onSignedOut()
        fun onSignInFailed(statusCode: Int)
    }

    companion object {
        const val REQUEST_CODE_SIGN_IN = 1
        private const val CLIENT_ID = "536672052707-hil8ei6h2m1e6aktfqhva5cjmpk6raoj.apps.googleusercontent.com"
        fun getMessageForStatusCode(context: Context, statusCode: Int): String = when (statusCode) {
            com.google.android.gms.common.api.CommonStatusCodes.NETWORK_ERROR ->
                context.getString(R.string.sign_in_network_error)
            else ->
                context.getString(R.string.sign_in_unknown_error, statusCode)
        }
    }

    var token: String? = null

    private val gso: GoogleSignInOptions
    private val client: GoogleSignInClient
    private var lastSignedInAccount: GoogleSignInAccount? = null

    init {
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(CLIENT_ID)
                .build()
        client = GoogleSignIn.getClient(context, gso)
    }

    open fun isSignedIn() = lastSignedInAccount != null
    open fun getAccountName(): String = lastSignedInAccount?.email ?: ""

    /**
     * Tries to sign in the user. If the user is already signed in and the idToken is
     * valid it will complete immediately. If the user is signed in but the idToken
     * has expired it will refresh the token asynchronously. If the user is not signed
     * in handleSignInResult will fail with GoogleSignInStatusCodes.SIGN_IN_REQUIRED.
     *
     * Only tries to sign in if a new enough version of Google play services is installed.
     *
     * @param activity If onStop is called on the given activity the login will be aborted
     * @param onSuccess function to run once the login has finished successfully
     */
    open fun silentSignIn(activity: Activity, onSuccess: (() -> Unit)?) {
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity) !=
                com.google.android.gms.common.api.CommonStatusCodes.SUCCESS) {
            // If Google play services doesn't exist we know the attempt will fail
            // so don't do anything in that case
            return
        }

        check(onSuccess == null || onSignInSuccessCallback == null) {
            "onSignInSuccessCallback is not null"
        }
        onSignInSuccessCallback = onSuccess
        client.silentSignIn().addOnCompleteListener(activity, { completedTask ->
            handleSignInResult(completedTask)
        })
    }

    private var onSignInSuccessCallback: (() -> Unit)? = null

    open fun signIn(activity: Activity, onSuccess: (() -> Unit)?) {
        val availability = GoogleApiAvailability.getInstance()
        val statusCode = availability.isGooglePlayServicesAvailable(activity)
        // Have the system show an error dialog if Google play services isn't installed
        val errorDialogShown = availability.showErrorDialogFragment(activity, statusCode, 0)
        if (!errorDialogShown) {
            // Only try to sign in if Google play services is installed.
            onSignInSuccessCallback = onSuccess
            activity.startActivityForResult(client.signInIntent, REQUEST_CODE_SIGN_IN)
        }
    }

    open fun signOut() {
        client.signOut()
        lastSignedInAccount = null
        token = null
        listener.onSignedOut()
        ParkApp.analytics.setProperty(UserProperties.propertySignedIn, UserProperties.valueNo)
    }

    fun revokeAccess() {
        // forget me and delete all data stored (need to delete data on the server)
        signOut()
        client.revokeAccess()
    }

    /**
     * Does a silent sign in and if it succeeds it calls the given function with
     * the newly updated token (which is guaranteed to be not null).
     *
     * @param activity If onStop is called on the given activity the login will be aborted
     * @param doWith function to run with the token as parameter once the login
     * has finished successfully
     */
    fun doWithFreshToken(activity: Activity, doWith: (String) -> Unit) {
        silentSignIn(activity, { doWith(token!!) })
    }

    fun onSignInResult(data: Intent?) {
        handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(data))
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            lastSignedInAccount = account
            token = account!!.idToken // todo fix null
            listener.onSignedIn()
            ParkApp.analytics.setProperty(UserProperties.propertySignedIn, UserProperties.valueYes)
            // Do the success callback as a new message so the sign in process can finish completely
            val callback = onSignInSuccessCallback
            if (callback != null) {
                Handler().post { callback.invoke() }
            }
        } catch (e: ApiException) {
            lastSignedInAccount = null
            token = null
            when (e.statusCode) {
                com.google.android.gms.common.api.CommonStatusCodes.SIGN_IN_REQUIRED -> {
                    // Silent sign in attempt and user is not signed in. No need to
                    // notify user about that.
                }
                com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes.SIGN_IN_CANCELLED,
                com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS -> {
                    // Failures during sign in that can be silently ignored.
                }
                else -> {
                    // Normal sign in attempt failed
                    listener.onSignInFailed(e.statusCode)
                }
            }
        }
        onSignInSuccessCallback = null
    }
}