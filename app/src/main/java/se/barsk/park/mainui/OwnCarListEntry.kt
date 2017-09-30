package se.barsk.park.mainui

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import se.barsk.park.R
import se.barsk.park.datatypes.Car
import se.barsk.park.datatypes.OwnCar


/**
 * One entry in the list of own cars
 */
class OwnCarListEntry(context: Context?, val listener: ((Car) -> Unit)?) : RelativeLayout(context), CarListEntry {
    constructor(context: Context?) : this(context, null)

    private val parkButton: Button by lazy { findViewById<Button>(R.id.park_button) }

    init {
        LayoutInflater.from(context).inflate(R.layout.own_car_entry, this, true)
        // The button handles it's clicks on it's own. Disable the list entry so it doesn't
        // fire any click events to the view holder/adapter.
        isEnabled = false
    }

    override fun showItem(car: Car, selected: Boolean) {
        car as OwnCar
        val park = if (car.parked) {
            context.getString(R.string.unpark_label)
        } else {
            context.getString(R.string.park_label)
        }
        val firstLen = park.length
        val spannable = SpannableString("${park.toUpperCase()}\n${car.regNo}")
        spannable.setSpan(RelativeSizeSpan(0.67f), 0, firstLen, 0);
        parkButton.text = spannable
        parkButton.setOnClickListener { listener?.invoke(car) }

        if (car.parked) {
            setBackroundPreservePadding(parkButton, R.drawable.bg_unpark_button)
            parkButton.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
        } else {
            setBackroundPreservePadding(parkButton, R.drawable.bg_park_button)
            parkButton.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
        }
    }

    private fun setBackroundPreservePadding(view: View, background: Int) {
        val pL = view.paddingLeft
        val pT = view.paddingTop
        val pR = view.paddingRight
        val pB = view.paddingBottom

        view.setBackgroundResource(background)
        view.setPadding(pL, pT, pR, pB)
    }
}