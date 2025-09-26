package se.barsk.park.mainui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import se.barsk.park.ParkApp
import se.barsk.park.R
import se.barsk.park.utils.Utils

// OK to ignore on Alert dialogs
// https://wundermanthompsonmobile.com/2013/05/layout-inflation-as-intended/
@SuppressLint("InflateParams")
class SpecifyServerDialog : DialogFragment() {

    interface SpecifyServerDialogListener {
        fun parkServerChanged()
    }

    companion object {
        fun newInstance(): SpecifyServerDialog = SpecifyServerDialog()
    }

    private lateinit var listener: SpecifyServerDialogListener
    private val dialogView: View by lazy {
        requireActivity().layoutInflater.inflate(R.layout.specify_server_dialog, null) as View
    }
    // also used by tests
    val editText: EditText by lazy { dialogView.findViewById(R.id.server_url_input) }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            activity as SpecifyServerDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(activity.toString() + " must implement SpecifyServerDialogListener")
        }
    }

    override fun onStart() {
        super.onStart()
        updatePositiveButton()
        editText.addTextChangedListener(DialogTextWatcher())
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.specify_server_dialog_title)
                .setView(dialogView)
                .setNegativeButton(R.string.dialog_button_cancel) { _, _ ->
                    requireDialog().cancel()
                }
                .setPositiveButton(R.string.dialog_button_ok) { _, _: Int ->
                    ParkApp.storageManager.setServer(editText.text.toString())
                    listener.parkServerChanged()
                }
                .create()
        editText.setOnEditorActionListener { _, _, _ ->
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
            false
        }
        editText.setText(ParkApp.storageManager.getServer())
        // The activity is visible when the dialog is created so the window should never be null
        dialog.window!!.let {
            it.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams .FLAG_ALT_FOCUSABLE_IM)
            it.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }

        return dialog
    }

    private fun updatePositiveButton() {
        val canSave = Patterns.WEB_URL.matcher(Utils.fixUrl(editText.text.toString())).matches()
        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = canSave
    }

    inner class DialogTextWatcher : TextWatcher {
        override fun afterTextChanged(s: Editable?) = updatePositiveButton()
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
    }
}