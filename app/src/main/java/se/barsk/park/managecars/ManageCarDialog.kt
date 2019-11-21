package se.barsk.park.managecars

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import se.barsk.park.R
import se.barsk.park.datatypes.OwnCar

// OK to ignore on Alert dialogs
// https://wundermanthompsonmobile.com/2013/05/layout-inflation-as-intended/
@SuppressLint("InflateParams")
abstract class ManageCarDialog : DialogFragment() {
    interface ManageCarDialogListener {
        fun onDialogPositiveClick(newCar: OwnCar, dialogType: DialogType)
    }

    abstract val dialogTitle: Int
    abstract val dialogType: DialogType
    abstract fun fetchOwnCar(): OwnCar

    var listener: ManageCarDialogListener? = null

    private val dialogView: View by lazy { requireActivity().layoutInflater.inflate(R.layout.manage_car_dialog, null) as View }
    private val fab: FloatingActionButton by lazy { requireActivity().findViewById<FloatingActionButton>(R.id.manage_cards_fab) }
    private val regNoView: EditText by lazy { dialogView.findViewById<EditText>(R.id.regno) }
    private val ownerView: EditText by lazy { dialogView.findViewById<EditText>(R.id.owner) }
    private val ownCar: OwnCar by lazy { fetchOwnCar() }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            activity as ManageCarDialogListener
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
        fab.hide()
    }

    override fun onStop() {
        super.onStop()
        fab.show()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireContext())
        regNoView.setText(ownCar.regNo)
        ownerView.setText(ownCar.owner)

        builder.setView(dialogView)
                .setTitle(dialogTitle)
                .setNegativeButton(R.string.dialog_button_cancel) { _, _ -> }
                .setPositiveButton(R.string.dialog_button_save) { _, _ ->
                    val newCar = OwnCar(
                            regNoView.text.toString().trim(),
                            ownerView.text.toString().trim(),
                            ownCar.nickName,
                            ownCar.id
                    )
                    listener?.onDialogPositiveClick(newCar, dialogType)
                }
        val dialog = builder.create()
        ownerView.setOnEditorActionListener { _, _, _ ->
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
            false
        }
        // The dialog is visible during onCreateDialog so safe to assume that the window is not null
        dialog.window!!.let {
            it.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            it.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }

        return dialog
    }

    private fun updatePositiveButton() {
        val canSave = regNoView.text.isNotBlank() && ownerView.text.isNotBlank()
        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = canSave
    }

    inner class DialogTextWatcher : TextWatcher {
        override fun afterTextChanged(s: Editable?) = updatePositiveButton()
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
    }

    enum class DialogType {
        EDIT, ADD
    }
}