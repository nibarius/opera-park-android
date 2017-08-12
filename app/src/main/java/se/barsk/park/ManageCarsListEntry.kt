package se.barsk.park

import android.content.Context
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView

/**
 * One entry in the manage cars list
 */
class ManageCarsListEntry(context: Context?) : RelativeLayout(context), CarListEntry {
    private val regNoView: TextView by lazy { findViewById(R.id.manage_cars_entry) as TextView }

    init {
        LayoutInflater.from(context).inflate(R.layout.manage_cars_entry, this, true)
    }

    override fun showItem(car: Car) {
        car as OwnCar
        regNoView.text = "${car.regNo} (owner: ${car.owner})"
    }
}