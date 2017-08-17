package se.barsk.park.manage_cars

import se.barsk.park.R
import se.barsk.park.datatypes.OwnCar

class EditCarDialog(ownCar: OwnCar? = null) : ManageCarDialog(ownCar!!) {
    override val dialogTitle: Int
        get() = R.string.edit_car_dialog_title

    override val dialogType: DialogType
        get() = DialogType.EDIT
}