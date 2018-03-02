package se.barsk.park

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

// Google documentation for sign-in: https://developers.google.com/identity/sign-in/android/start

/**
 * Handler for signing in to the app.
 */
class SignInHandler(val context: Context) {
    var token: String? = null

    companion object {
        val REQUEST_CODE_SIGN_IN = 1
        private val CLIENT_ID = "536672052707-hil8ei6h2m1e6aktfqhva5cjmpk6raoj.apps.googleusercontent.com"
    }

    private val gso: GoogleSignInOptions
    private val client: GoogleSignInClient
    private var lastSignedInAccount: GoogleSignInAccount?

    init {
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(CLIENT_ID)
                .build()
        client = GoogleSignIn.getClient(context, gso)
        lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(context)
    }

    // lastSignedInAccount returns null if the user is not already signed in
    fun isSignedIn() = lastSignedInAccount != null

    fun getPersonName(): String {
        val account = lastSignedInAccount
        return if (account == null) {
            "Sign in"
        } else {
            "Sign out " + account.email
        }
    }

    fun silentSignIn(activity: Activity) {
        client.silentSignIn().addOnCompleteListener(activity, { task -> handleSignInResult(task) })
    }

    fun signIn(activity: Activity) {
        val signInIntent = client.signInIntent
        activity.startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN)
    }

    fun signOut() {
        // todo: remove from wait list if on the wait list.
        client.signOut()
        lastSignedInAccount = null
        token = null
        ParkApp.theUser.signedIn = false //todo: a listener instead of global access to the user?
    }

    fun revokeAccess() {
        // forget me and delete all data stored (need to delete data on the server)
        signOut()
        client.revokeAccess()
    }

    fun onSignInResult(data: Intent) {
        // The Task returned from this call is always completed, no need to attach a listener.
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        handleSignInResult(task)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            lastSignedInAccount = account
            token = account.idToken
            ParkApp.theUser.signedIn = true //todo: a listener instead of global access to the user?
        } catch (e: ApiException) {
            // If the user is not signed in (silent sign in) or the sign in fails we end up here.
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            // Also code = 4 means silent sign in and not signed in.
        }
    }
}