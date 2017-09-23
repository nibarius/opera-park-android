package se.barsk.park.main_ui

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.webkit.URLUtil
import android.widget.EditText
import se.barsk.park.R
import se.barsk.park.Utils
import se.barsk.park.storage.StorageManager


class SpecifyServerDialog : DialogFragment() {

    interface SpecifyServerDialogListener {
        fun parkServerChanged()
        fun parkServerDialogCancelled()
    }

    companion object {
        fun newInstance(): SpecifyServerDialog = SpecifyServerDialog()
    }

    private lateinit var listener: SpecifyServerDialogListener
    private val dialogView: View by lazy {
        activity.layoutInflater.inflate(R.layout.specify_server_dialog, null) as View
    }
    private val editText: EditText by lazy { dialogView.findViewById<EditText>(R.id.server_url_input) }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = activity as SpecifyServerDialogListener
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

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        listener.parkServerDialogCancelled()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(activity)
                .setTitle("Specify park server")
                .setView(dialogView)
                .setNegativeButton(R.string.cancel, { _, _ ->
                    dialog.cancel()
                })
                .setPositiveButton("OK", { _, _: Int ->
                    StorageManager.setServer(editText.text.toString())
                    listener.parkServerChanged()
                })
                .create()
        editText.setOnEditorActionListener({ _, _, _ ->
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
            false
        })
        editText.setText(StorageManager.getServer())
        dialog.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        return dialog
    }

    private fun updatePositiveButton() {
        val canSave = URLUtil.isValidUrl(Utils.fixUrl(editText.text.toString()))
        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = canSave
    }

    inner class DialogTextWatcher() : TextWatcher {
        override fun afterTextChanged(s: Editable?) = updatePositiveButton()
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }
}