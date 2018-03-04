package se.barsk.park.mainui

import android.support.design.widget.Snackbar
import android.view.View
import android.widget.TextView

/**
 * Common implementation for all error messages. Error's are shown as a simple snackbar.
 * @param containerView The view in which the message should be shown.
 */
class ErrorMessage(private val containerView: View) {
    fun show(message: String) {
        val snackbar = Snackbar
                .make(containerView, message, Snackbar.LENGTH_LONG)
                .setAction("Action", null)
        val textView = snackbar.view.findViewById<TextView>(android.support.design.R.id.snackbar_text)
        textView.maxLines = 5
        snackbar.show()
    }
}