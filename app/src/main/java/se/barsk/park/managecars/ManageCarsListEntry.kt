package se.barsk.park.managecars

import android.animation.Animator
import android.content.Context
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
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
class ManageCarsListEntry(context: Context) : RelativeLayout(context, null, R.attr.manageCarsEntryStyle), CarListEntry {
    private val regNoView: TextView by lazy { findViewById<TextView>(R.id.manage_cars_entry) }
    private val avatarTextView: TextView by lazy { findViewById<TextView>(R.id.avatar_text_view) }
    private val avatarCheckView: ImageView by lazy { findViewById<ImageView>(R.id.avatar_check_view) }
    private val selectedColor = ContextCompat.getColor(context, R.color.colorPrimary)
    private var unselectedColor = ContextCompat.getColor(context, R.color.colorPrimary)
    private var hideAnimation: Animator? = null
    private val animationDuration: Long = 300

    init {
        LayoutInflater.from(context).inflate(R.layout.manage_cars_entry, this, true)
    }

    override fun showItem(car: Car, selected: Boolean, garageFull: Boolean) {
        car as OwnCar
        regNoView.text = context.getString(R.string.manage_cars_list_entry, car.regNo, car.owner)

        unselectedColor = getColorForCar(car, context)
        avatarTextView.text = car.regNo.substring(0, 1)
        setAvatarColor(unselectedColor, avatarTextView)
        setAvatarColor(selectedColor, avatarCheckView)

        if (selected) {
            select()
        } else {
            deselect()
        }
    }

    fun select() {
        circularReveal(avatarCheckView)
    }

    fun deselect() {
        if (avatarCheckView.visibility != View.INVISIBLE) {
            circularHide(avatarCheckView)
        }
    }

    private fun setAvatarColor(color: Int, view: View) = setAvatarColor(color, context, view)

    // Do a circular review animation for the check view.
    private fun circularReveal(view: View) {
        val finalRadius = Math.max(view.width, view.height).toFloat()

        val circularReveal = ViewAnimationUtils.createCircularReveal(view, 0, 0, 0f, finalRadius * 1.4f)
        circularReveal.duration = animationDuration
        circularReveal.addListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(p0: Animator?) = Unit
            override fun onAnimationRepeat(p0: Animator?) = Unit
            override fun onAnimationCancel(p0: Animator?) = Unit
            override fun onAnimationStart(p0: Animator?) {
                if (hideAnimation != null) {
                    // Cancel the hide animation if it's currently animating so that the
                    // check view don't get hidden again when the old hide animation finishes.
                    hideAnimation?.cancel()
                    hideAnimation = null
                }
            }
        })

        // Make the check view visible and start animating
        view.visibility = View.VISIBLE
        circularReveal.start()
    }


    // Do a circular hide animation for the check view.
    private fun circularHide(view: View) {
        val finalRadius = Math.max(view.width, view.height).toFloat()

        val animation = ViewAnimationUtils.createCircularReveal(view, 0, 0, finalRadius * 1.4f, 0f)
        animation.duration = animationDuration
        animation.addListener(object : Animator.AnimatorListener {
            var cancelled = false
            override fun onAnimationEnd(p0: Animator?) {
                // If the animation hasn't been cancelled the check view should be hidden
                // now that the animation has been completed.
                if (!cancelled) {
                    view.visibility = View.INVISIBLE
                }
                hideAnimation = null
            }

            override fun onAnimationCancel(p0: Animator?) {
                cancelled = true
            }

            override fun onAnimationRepeat(p0: Animator?) = Unit
            override fun onAnimationStart(p0: Animator?) = Unit
        })

        animation.start()
        hideAnimation = animation
    }
}