package se.barsk.park.error

import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import se.barsk.park.BuildConfig
import se.barsk.park.R

/**
 * A handler for various error cases.
 */
object ErrorHandler {
    /**
     * Triggers an assert in debug builds and does nothing in non debug builds.
     * @param message The message describing the exception
     * @param exception The Exception that was thrown.
     */
    fun raiseException(message: String, exception: Exception? = null) {
        if (BuildConfig.DEBUG) {
            assert(false) { message + "\n" + exception.toString() }
        }
    }

    /**
     * Common implementation for all error messages. Error's are shown as a simple snackbar.
     * @param containerView The view in which the message should be shown.
     * @param message The message to show.
     */
    fun showMessage(containerView: View, message: String) {
        val snackbar = Snackbar
            .make(containerView, message, Snackbar.LENGTH_LONG)
            .setAction("Action", null)
        val textView = snackbar.view.findViewById<TextView>(R.id.snackbar_text)
        textView.maxLines = 5
        snackbar.show()
    }
}
