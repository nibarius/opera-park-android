package se.barsk.park.manage_cars

import se.barsk.park.R

class AddCarDialog : EditCarDialog() {
    override val dialogTitle: Int
        get() = R.string.add_car_dialog_title
}