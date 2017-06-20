package se.barsk.park

import android.content.Context
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView

/**
 * One entry in the list of own parked cars
 */
class OwnCarListEntry(context: Context?) : RelativeLayout(context), CarListEntry {
    private var regNoView: TextView? = null
    private var actionLabelView: TextView? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.own_car_entry, this, true)
        regNoView = findViewById(R.id.regno) as TextView?
        actionLabelView = findViewById(R.id.action_label) as TextView?
    }

    override fun showItem(car: Car) {
        car as OwnCar
        regNoView?.text = car.regNo
        actionLabelView?.text = "Park"
    }
}