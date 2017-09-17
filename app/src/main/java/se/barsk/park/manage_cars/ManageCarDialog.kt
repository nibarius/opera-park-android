package se.barsk.park.manage_cars

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import se.barsk.park.R
import se.barsk.park.datatypes.OwnCar

abstract class ManageCarDialog() : DialogFragment() {
    interface ManageCarDialogListener {
        fun onDialogPositiveClick(newCar: OwnCar, dialogType: DialogType)
    }

    abstract val dialogTitle: Int
    abstract val dialogType: DialogType
    abstract fun fetchOwnCar(): OwnCar

    var listener: ManageCarDialogListener? = null
    val dialogView: View by lazy {
        activity.layoutInflater.inflate(R.layout.manage_car_dialog, null) as View
    }
    val regNoView: EditText by lazy { dialogView.findViewById<EditText>(R.id.regno) }
    val ownerView: EditText by lazy { dialogView.findViewById<EditText>(R.id.owner) }
    val ownCar: OwnCar by lazy { fetchOwnCar() }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = activity as ManageCarDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(activity.toString() + " must implement ManageCarDialogListener")
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
        regNoView.setText(ownCar.regNo)
        ownerView.setText(ownCar.owner)

        builder.setView(dialogView)
                .setTitle(dialogTitle)
                .setNegativeButton(R.string.cancel, { _, _ -> })
                .setPositiveButton(R.string.save, { _, _ ->
                    val newCar = OwnCar(
                            regNoView.text.toString().trim(),
                            ownerView.text.toString().trim(),
                            ownCar.nickName,
                            ownCar.id
                    )
                    listener?.onDialogPositiveClick(newCar, dialogType)
                })
        val dialog = builder.create()
        ownerView.setOnEditorActionListener({ _, _, _ ->
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
            false
        })
        dialog.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        return dialog
    }

    private fun updatePositiveButton() {
        val canSave = !TextUtils.isEmpty(regNoView.text) && !TextUtils.isEmpty(ownerView.text)
        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = canSave
    }

    inner class DialogTextWatcher() : TextWatcher {
        override fun afterTextChanged(s: Editable?) = updatePositiveButton()
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    enum class DialogType {
        EDIT, ADD
    }

}