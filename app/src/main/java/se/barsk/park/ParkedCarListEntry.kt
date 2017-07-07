package se.barsk.park

import android.content.Context
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView

/**
 * One entry in the list of parked cars
 */
class ParkedCarListEntry(context: Context?) : RelativeLayout(context), CarListEntry {
    private val parkTimeView: TextView by lazy { findViewById(R.id.park_start_time) as TextView }
    private val regNoView: TextView by lazy { findViewById(R.id.action) as TextView }

    init {
        LayoutInflater.from(context).inflate(R.layout.parked_car_entry, this, true)
    }

    override fun showItem(car: Car) {
        car as ParkedCar
        parkTimeView.text = car.startTime.substring(11, 19) // Skip the date part
        regNoView.text = "${car.regNo} (${car.owner})"
    }
}