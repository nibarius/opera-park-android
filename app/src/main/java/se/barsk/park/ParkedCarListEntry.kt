package se.barsk.park

import android.content.Context
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView

/**
 * One entry in the list of parked cars
 */
class ParkedCarListEntry(context: Context?) : RelativeLayout(context), CarListEntry {
    private var parkTimeView: TextView? = null
    private var regNoView: TextView? = null
    private var ownerView: TextView? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.parked_car_entry, this, true)
        parkTimeView = findViewById(R.id.park_start_time) as TextView?
        regNoView = findViewById(R.id.action_label) as TextView?
        ownerView = findViewById(R.id.owner) as TextView?
    }

    override fun showItem(car: Car) {
        car as ParkedCar
        parkTimeView?.text = car.startTime
        regNoView?.text = car.regNo
        ownerView?.text = car.owner
    }
}