package se.barsk.park.managecars

import android.os.Bundle
import se.barsk.park.ParkApp
import se.barsk.park.R
import se.barsk.park.datatypes.OwnCar


class EditCarDialog : ManageCarDialog() {

    override val dialogTitle: Int
        get() = R.string.edit_car_dialog_title

    override val dialogType: DialogType
        get() = DialogType.EDIT

    override fun fetchOwnCar(): OwnCar {
        // This dialog always have a Bundle with one parameter when set up as expected
        val carId = requireArguments().getString(ARG_ID)!!
        return ParkApp.carCollection.getCar(carId)
    }

    companion object {
        private const val ARG_ID: String = "id"
        fun newInstance(carId: String): EditCarDialog {
            val dialog = EditCarDialog()
            val args = Bundle()
            args.putString(ARG_ID, carId)
            dialog.arguments = args
            return dialog
        }
    }
}