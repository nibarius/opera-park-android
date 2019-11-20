package se.barsk.park.mainui

import android.content.Context
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import se.barsk.park.ParkApp
import se.barsk.park.R
import se.barsk.park.datatypes.Car
import se.barsk.park.datatypes.OwnCar
import se.barsk.park.utils.Utils
import java.util.*


/**
 * One entry in the list of own cars
 */
class OwnCarListEntry(context: Context?, val listener: ((Car) -> Unit)?) : RelativeLayout(context, null, R.attr.ownCarEntryStyle), CarListEntry {
    constructor(context: Context?) : this(context, null)

    private val parkButton: MaterialButton by lazy { findViewById<MaterialButton>(R.id.park_button) }

    init {
        LayoutInflater.from(context).inflate(R.layout.own_car_entry, this, true)
        // The button handles it's clicks on it's own. Disable the list entry so it doesn't
        // fire any click events to the view holder/adapter.
        isEnabled = false
    }

    override fun showItem(car: Car, selected: Boolean, garageFull: Boolean) {
        car as OwnCar
        val action = if (car.parked) {
            context.getString(R.string.unpark_label)
        } else if (!garageFull) {
            context.getString(R.string.park_label)
        } else if (ParkApp.theUser.isOnWaitList) {
            context.getString(R.string.stop_waiting_label)
        } else {
            context.getString(R.string.wait_label)
        }
        val firstLen = action.length
        val spannable = SpannableString("${action.toUpperCase(Locale.getDefault())}\n${car.regNo}")
        spannable.setSpan(RelativeSizeSpan(0.67f), 0, firstLen, 0)
        parkButton.text = spannable
        parkButton.setOnClickListener { listener?.invoke(car) }

        when {
            car.parked -> { // Show unpark button
                parkButton.strokeColor = ContextCompat.getColorStateList(context, R.color.button_unpark_bg)
                if (Utils.isDarkTheme(context)) {
                    // Use text button style with transparent background in dark mode
                    parkButton.backgroundTintList = ContextCompat.getColorStateList(context, R.color.button_transparent_bg)
                    parkButton.rippleColor = ContextCompat.getColorStateList(context, R.color.button_unpark_ripple)
                    parkButton.setTextColor(ContextCompat.getColor(context, R.color.unparkButtonColor))
                } else {
                    // Normal mode uses a high emphasis button for unparking
                    parkButton.backgroundTintList = ContextCompat.getColorStateList(context, R.color.button_unpark_bg)
                    parkButton.rippleColor = ContextCompat.getColorStateList(context, R.color.button_ripple_on_primary)
                    parkButton.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                }
            }
            garageFull -> { // Show wait button
                parkButton.backgroundTintList = ContextCompat.getColorStateList(context, R.color.button_transparent_bg)
                parkButton.strokeColor = ContextCompat.getColorStateList(context, R.color.button_wait_stroke)
                parkButton.rippleColor = ContextCompat.getColorStateList(context, R.color.button_wait_ripple)
                parkButton.setTextColor(ContextCompat.getColor(context, R.color.waitButtonColor))
            }
            else -> { // Show park button
                parkButton.backgroundTintList = ContextCompat.getColorStateList(context, R.color.button_transparent_bg)
                parkButton.strokeColor = ContextCompat.getColorStateList(context, R.color.button_park_stroke)
                parkButton.rippleColor = ContextCompat.getColorStateList(context, R.color.button_park_ripple)
                parkButton.setTextColor(ContextCompat.getColor(context, R.color.parkButtonColor))
            }
        }
    }
}