package se.barsk.park

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView

/**
 * One entry in the list of own parked cars
 */
class OwnCarListEntry(context: Context?) : RelativeLayout(context), CarListEntry {
    private val regNoView: TextView by lazy { findViewById(R.id.regno) as TextView }
    private val actionLabelView: TextView by lazy { findViewById(R.id.own_car_label) as TextView }
    private val ownerLabelView: TextView by lazy { findViewById(R.id.owner) as TextView }

    init {
        LayoutInflater.from(context).inflate(R.layout.own_car_entry, this, true)
    }

    override fun showItem(car: Car, selected: Boolean) {
        car as OwnCar
        regNoView.text = car.regNo
        if (car.parked) {
            actionLabelView.text = "Unpark"
            setBackgroundColor(ContextCompat.getColor(context, R.color.colorParkedCar))
        } else {
            actionLabelView.text = "Park"
            setBackgroundColor(ContextCompat.getColor(context, R.color.colorUnparkedCar))
        }
        ownerLabelView.text = "(as ${car.owner})"
    }
}