package se.barsk.park.mainui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog
import se.barsk.park.R

/**
 * A dialog shown to the user when they try to do an action that
 * requires signing in.
 */
class MustSignInDialog : DialogFragment() {
    companion object {
        fun newInstance() = MustSignInDialog()
    }

    interface MustSignInDialogListener {
        fun onSignInDialogPositiveClick()
    }

    var listener: MustSignInDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = try {
            // Instantiate the MustSignInDialogListener so we can send events to the host
            activity as MustSignInDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(activity.toString() + " must implement MustSignInDialogListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle(getString(R.string.sign_in_dialog_title))
                .setMessage(getString(R.string.sign_in_dialog_message))
                .setNegativeButton(R.string.dialog_button_cancel, { _, _ -> })
                .setPositiveButton(getString(R.string.sign_in_dialog_positive_button), { _, _ -> listener?.onSignInDialogPositiveClick() })
        return builder.create()
    }
}