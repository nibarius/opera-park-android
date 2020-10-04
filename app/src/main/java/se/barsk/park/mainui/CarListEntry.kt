package se.barsk.park.mainui

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import se.barsk.park.R
import se.barsk.park.datatypes.Car
import kotlin.math.abs


/**
 * An interface for all lists of cars (parked cars and own cars)
 */
interface CarListEntry {
    fun showItem(car: Car, selected: Boolean = false, garageFull: Boolean = false)

    fun setAvatarColor(car: Car, context: Context, view: View) =
            setAvatarColor(getColorForCar(car, context), context, view)

    fun setAvatarColor(color: Int, context: Context, view: View) {
        val drawable = ContextCompat.getDrawable(context, R.drawable.circle_drawable)!!
        drawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(color, BlendModeCompat.SRC_ATOP)
        setBackgroundPreservePadding(view, drawable)
    }

    fun getColorForCar(car: Car, context: Context): Int {
        val colors = context.resources.getIntArray(R.array.avatar_background_color)
        return colors[abs(car.regNo.hashCode()) % colors.size]
    }

    private fun setBackgroundPreservePadding(view: View, background: Drawable) {
        val pL = view.paddingLeft
        val pT = view.paddingTop
        val pR = view.paddingRight
        val pB = view.paddingBottom

        view.background = background
        view.setPadding(pL, pT, pR, pB)
    }
}