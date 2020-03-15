package se.barsk.park.mainui

import android.content.Context
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import se.barsk.park.R
import se.barsk.park.datatypes.Car
import se.barsk.park.datatypes.ParkedCar
import se.barsk.park.utils.Utils.fixRegnoForDisplay

/**
 * One entry in the list of parked cars
 */
class ParkedCarListEntry(context: Context?) : RelativeLayout(context), CarListEntry {
    private val parkTextView: TextView by lazy { findViewById<TextView>(R.id.parked_entry_text) }
    private val avatarTextView: TextView by lazy { findViewById<TextView>(R.id.avatar_text_view) }


    init {
        LayoutInflater.from(context).inflate(R.layout.parked_car_entry, this, true)
    }

    override fun showItem(car: Car, selected: Boolean, garageFull: Boolean) {
        car as ParkedCar

        val firstLine = "${fixRegnoForDisplay(car.regNo)} - ${car.owner}"
        val secondLine = if (car.startTime.length >= 19) {
            car.startTime.substring(11, 19) // Skip the date part
        } else {
            car.startTime
        }
        val spannable = SpannableString("$firstLine\n$secondLine")
        spannable.setSpan(AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.parked_cars_text_size)), 0, firstLine.length, 0)
        spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.highEmphasisText)), 0, firstLine.length, 0)
        parkTextView.text = spannable

        setAvatarColor(car, context, avatarTextView)
        avatarTextView.text = if (car.regNo.isNotEmpty()) car.regNo.substring(0, 1) else "?"
    }
}