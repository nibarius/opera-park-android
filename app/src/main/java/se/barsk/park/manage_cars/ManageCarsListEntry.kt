package se.barsk.park.manage_cars

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import se.barsk.park.datatypes.Car
import se.barsk.park.main_ui.CarListEntry
import se.barsk.park.datatypes.OwnCar
import se.barsk.park.R


/**
 * One entry in the manage cars list
 */
class ManageCarsListEntry(context: Context?) : RelativeLayout(context), CarListEntry {
    private val regNoView: TextView by lazy { findViewById(R.id.manage_cars_entry) as TextView }
    private val avatarFrame: FrameLayout by lazy { findViewById(R.id.avatar_frame_layout) as FrameLayout }
    private val avatarTextView: TextView by lazy { findViewById(R.id.avatar_text_view) as TextView }
    private val avatarCheckView: ImageView by lazy { findViewById(R.id.avatar_check_view) as ImageView }
    private val selectedColor = ContextCompat.getColor(context, R.color.colorPrimary)
    private var unselectedColor = ContextCompat.getColor(context, R.color.colorPrimary)

    init {
        LayoutInflater.from(context).inflate(R.layout.manage_cars_entry, this, true)
    }

    override fun showItem(car: Car, selected: Boolean) {
        car as OwnCar
        regNoView.text = "${car.regNo} (owner: ${car.owner})"

        val colors = context.resources.getIntArray(R.array.avatar_background_color)
        unselectedColor = colors[Math.abs(car.regNo.hashCode()) % colors.size]
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

    private fun setAvatarColor(color: Int) {
        val drawable = ContextCompat.getDrawable(context, R.drawable.circle_drawable)
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        avatarFrame.background = drawable
        avatarFrame.setPadding(0, 0, 0, 0) // setting background resets padding, remove it again
    }

}