package se.barsk.park

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.EditText

open class EditCarDialog(val ownCar: OwnCar? = null) : DialogFragment() {
    interface EditCarDialogListener {
        fun onDialogPositiveClick(newCar: OwnCar)
    }

    open val dialogTitle = R.string.edit_car_dialog_title

    var listener: EditCarDialogListener? = null
    val dialogView: View by lazy {
        activity.layoutInflater.inflate(R.layout.manage_car_dialog, null) as View
    }
    val regNoView: EditText by lazy { dialogView.findViewById(R.id.regno) as EditText }
    val ownerView: EditText by lazy { dialogView.findViewById(R.id.owner) as EditText }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = activity as EditCarDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(activity.toString() + " must implement EditCarDialogListener")
        }
    }

    override fun onStart() {
        super.onStart()
        updatePositiveButton()
        val dialogWatcher = DialogTextWatcher()
        regNoView.addTextChangedListener(dialogWatcher)
        ownerView.addTextChangedListener(dialogWatcher)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        if (ownCar != null) {
            regNoView.setText(ownCar.regNo)
            ownerView.setText(ownCar.owner)
        }
        builder.setView(dialogView)
                .setTitle(dialogTitle)
                .setNegativeButton(R.string.cancel, { _, _ -> })
                .setPositiveButton(R.string.save, { _, _ ->
                    val newCar = OwnCar(regNoView.text.toString(), ownerView.text.toString())
                    listener?.onDialogPositiveClick(newCar)})
        val dialog = builder.create()
        dialog.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        return dialog
    }

    private fun updatePositiveButton() {
        val canSave = !TextUtils.isEmpty(regNoView.text) && !TextUtils.isEmpty(ownerView.text)
        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = canSave
    }

    inner class DialogTextWatcher(): TextWatcher {
        override fun afterTextChanged(s: Editable?) = updatePositiveButton()
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

}