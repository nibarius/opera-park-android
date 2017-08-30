package se.barsk.park.main_ui

import android.content.Context
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import se.barsk.park.R
import se.barsk.park.datatypes.Car
import se.barsk.park.datatypes.OwnCar

/**
 * One entry in the list of own parked cars
 */
class OwnCarListEntry(context: Context?) : RelativeLayout(context), CarListEntry {
    private val regNoView: TextView by lazy { findViewById(R.id.regno) as TextView }
    private val actionLabelView: TextView by lazy { findViewById(R.id.action_label) as TextView }

    init {
        LayoutInflater.from(context).inflate(R.layout.own_car_entry, this, true)
    }

    override fun showItem(car: Car, selected: Boolean) {
        car as OwnCar
        regNoView.text = car.regNo
        if (car.parked) {
            actionLabelView.text = "Unpark"
        } else {
            actionLabelView.text = "Park"
        }
    }
}