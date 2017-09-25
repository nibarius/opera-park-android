package se.barsk.park.mainui

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.view.View
import se.barsk.park.R
import se.barsk.park.datatypes.Car

/**
 * An interface for all lists of cars (parked cars and own cars)
 */
interface CarListEntry {
    fun showItem(car: Car, selected: Boolean)

    fun setAvatarColor(car: Car, context: Context, view: View) {
        setAvatarColor(getColorForCar(car, context), context, view)
    }

    fun setAvatarColor(color: Int, context: Context, view: View) {
        val drawable = ContextCompat.getDrawable(context, R.drawable.circle_drawable)
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        view.background = drawable
        view.setPadding(0, 0, 0, 0) // setting background resets padding, remove it again
    }

    fun getColorForCar(car: Car, context: Context): Int {
        val colors = context.resources.getIntArray(R.array.avatar_background_color)
        return colors[Math.abs(car.regNo.hashCode()) % colors.size]
    }
}