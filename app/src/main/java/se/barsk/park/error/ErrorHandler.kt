package se.barsk.park.error

import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import se.barsk.park.R

/**
 * A handler for various error cases.
 */
object ErrorHandler {

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
