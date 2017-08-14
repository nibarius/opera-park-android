package se.barsk.park

import android.content.Context
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.widget.FrameLayout
import android.widget.ImageView


/**
 * One entry in the manage cars list
 */
class ManageCarsListEntry(context: Context?) : RelativeLayout(context), CarListEntry {
    private val regNoView: TextView by lazy { findViewById(R.id.manage_cars_entry) as TextView }
    private val avatarFrame: FrameLayout by lazy { findViewById(R.id.avatar_frame_layout) as FrameLayout }
    private val avatarTextView: TextView by lazy { findViewById(R.id.avatar_text_view) as TextView }
    private val avatarCheckView: ImageView by lazy { findViewById(R.id.avatar_check_view) as ImageView }

    init {
        LayoutInflater.from(context).inflate(R.layout.manage_cars_entry, this, true)
    }

    override fun showItem(car: Car, selected: Boolean) {
        car as OwnCar
        regNoView.text = "${car.regNo} (owner: ${car.owner})"

        val color: Int
        if (selected) {
            avatarTextView.visibility = GONE
            avatarCheckView.visibility = VISIBLE
            color = ContextCompat.getColor(context, R.color.colorPrimaryDark)
        } else {
            avatarTextView.visibility = VISIBLE
            avatarTextView.text = car.regNo.substring(0,1)
            avatarCheckView.visibility = GONE
            val colors = context.resources.getIntArray(R.array.avatar_background_color)
            color = colors[Math.abs(car.regNo.hashCode()) % colors.size]
        }
        setAvatarColor(color)
    }

    private fun setAvatarColor(color: Int) {
        val drawable = ContextCompat.getDrawable(context, R.drawable.circle_drawable)
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        val pl = avatarFrame.paddingLeft
        val pt = avatarFrame.paddingTop
        val pr = avatarFrame.paddingRight
        val pb = avatarFrame.paddingBottom
        avatarFrame.background = drawable
        avatarFrame.setPadding(pl, pt, pr, pb) // setting background resets padding, restore it
    }

}