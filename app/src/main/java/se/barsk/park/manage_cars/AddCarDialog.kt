package se.barsk.park.manage_cars

import se.barsk.park.R
import se.barsk.park.datatypes.OwnCar

class AddCarDialog : ManageCarDialog(OwnCar("", "")) {
    override val dialogTitle: Int
        get() = R.string.add_car_dialog_title

    override val dialogType: DialogType
        get() = DialogType.ADD
}