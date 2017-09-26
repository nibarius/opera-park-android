package se.barsk.park.managecars

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import se.barsk.park.R
import se.barsk.park.datatypes.Car
import se.barsk.park.datatypes.OwnCar
import se.barsk.park.mainui.CarListEntry


/**
 * One entry in the manage cars list
 */
class ManageCarsListEntry(context: Context?) : RelativeLayout(context), CarListEntry {
    private val regNoView: TextView by  lazy { findViewById<TextView>(R.id.manage_cars_entry) }
    private val avatarFrame: FrameLayout by lazy { findViewById<FrameLayout>(R.id.avatar_frame_layout) }
    private val avatarTextView: TextView by lazy { findViewById<TextView>(R.id.avatar_text_view) }
    private val avatarCheckView: ImageView by lazy { findViewById<ImageView>(R.id.avatar_check_view) }
    private val selectedColor = ContextCompat.getColor(context, R.color.colorPrimary)
    private var unselectedColor = ContextCompat.getColor(context, R.color.colorPrimary)

    init {
        LayoutInflater.from(context).inflate(R.layout.manage_cars_entry, this, true)
    }

    override fun showItem(car: Car, selected: Boolean) {
        car as OwnCar
        regNoView.text = context.getString(R.string.manage_cars_list_entry, car.regNo, car.owner)

        unselectedColor = getColorForCar(car, context)
        avatarTextView.text = car.regNo.substring(0, 1)

        if (selected) {
            select()
        } else {
            deselect()
        }
    }

    fun select() {
        avatarTextView.visibility = GONE
        avatarCheckView.visibility = VISIBLE
        setAvatarColor(selectedColor)
    }

    fun deselect() {
        avatarTextView.visibility = VISIBLE
        avatarCheckView.visibility = GONE
        setAvatarColor(unselectedColor)
    }

    private fun setAvatarColor(color: Int) = setAvatarColor(color, context, avatarFrame)
}