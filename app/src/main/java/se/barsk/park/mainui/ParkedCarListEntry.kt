package se.barsk.park.mainui

import android.content.Context
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import se.barsk.park.R
import se.barsk.park.datatypes.Car
import se.barsk.park.datatypes.ParkedCar

/**
 * One entry in the list of parked cars
 */
class ParkedCarListEntry(context: Context?) : RelativeLayout(context), CarListEntry {
    private val parkTimeView: TextView by lazy { findViewById<TextView>(R.id.park_start_time) }
    private val regNoView: TextView by lazy { findViewById<TextView>(R.id.regno) }
    private val avatarTextView: TextView by lazy { findViewById<TextView>(R.id.avatar_text_view) }


    init {
        LayoutInflater.from(context).inflate(R.layout.parked_car_entry, this, true)
    }

    override fun showItem(car: Car, selected: Boolean) {
        car as ParkedCar
        parkTimeView.text = car.startTime.substring(11, 19) // Skip the date part
        regNoView.text = "${car.regNo.toUpperCase()} - ${car.owner}"
        setAvatarColor(car, context, avatarTextView)
        avatarTextView.text = car.regNo.substring(0, 1)
    }
}