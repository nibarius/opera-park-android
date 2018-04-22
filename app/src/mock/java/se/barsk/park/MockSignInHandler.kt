package se.barsk.park

import android.app.Activity
import android.content.Context

/**
 * Mock implementation of the sign in handler
 */
class MockSignInHandler(context: Context, listener: StatusChangedListener) : SignInHandler(context, listener) {

    override fun silentSignIn(activity: Activity, onSuccess: (() -> Unit)?) {
        token = "token"
        listener.onSignedIn()
        onSuccess?.invoke()
    }

    override fun signIn(activity: Activity, onSuccess: (() -> Unit)?) {
        token = "token"
        listener.onSignedIn()
        onSuccess?.invoke()
    }

    override fun signOut() {
        token = null
        listener.onSignedOut()

    }

    override fun isSignedIn() = token != null
    override fun getAccountName() = "mt@gmail.com"
}
