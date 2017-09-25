package se.barsk.park.managecars

import se.barsk.park.R
import se.barsk.park.datatypes.OwnCar

class AddCarDialog : ManageCarDialog() {
    private val emptyCar = OwnCar("", "")
    override fun fetchOwnCar(): OwnCar = emptyCar

    override val dialogTitle: Int
        get() = R.string.add_car_dialog_title

    override val dialogType: DialogType
        get() = DialogType.ADD

    companion object {
        fun newInstance(): AddCarDialog {
            return AddCarDialog()
        }
    }
}