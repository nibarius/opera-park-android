package se.barsk.park.mainui

import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            drawable.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP);
        } else {
            // Todo: Switch to androidx core library once 1.2 is out of beta
            // https://stackoverflow.com/a/58314657/1730966
            @Suppress("DEPRECATION")
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
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