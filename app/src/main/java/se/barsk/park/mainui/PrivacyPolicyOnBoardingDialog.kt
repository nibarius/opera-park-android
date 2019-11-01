package se.barsk.park.mainui

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import se.barsk.park.ParkApp
import se.barsk.park.R


/**
 * A dialog shown to the user when starting the app the first time
 * that asks the user to opt in to statistics collection.
 */
class PrivacyPolicyOnBoardingDialog : DialogFragment() {
    companion object {
        fun newInstance() = PrivacyPolicyOnBoardingDialog()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val url = getString(R.string.url_privacy_statement)
        val message = getString(R.string.ppo_dialog_message, url).fromHtml()
        builder.setTitle(getString(R.string.ppo_dialog_title))
                .setMessage(message)
                .setNegativeButton(R.string.ppo_dialog_negative) { _, _ -> decline() }
                .setPositiveButton(getString(R.string.ppo_dialog_positive)) { _, _ -> accept() }
        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        // Make the dialog's TextView clickable
        (requireDialog().findViewById(android.R.id.message) as TextView).movementMethod =
                LinkMovementMethod.getInstance()
    }

    override fun show(manager: FragmentManager, tag: String?) {
        super.show(manager, tag)
        isCancelable = false
    }

    private fun accept() {
        ParkApp.storageManager.giveStatsConsent()
        ParkApp.analytics.optOutToggled()
        ParkApp.crashlytics.enableIfAllowed(requireContext())
        ParkApp.storageManager.setPrivacyOnBoardingSeen()
    }

    private fun decline() {
        ParkApp.storageManager.withdrawStatsConsent()
        ParkApp.storageManager.setPrivacyOnBoardingSeen()
    }

    private fun String.fromHtml(): Spanned {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            return Html.fromHtml(this)
        }
    }
}