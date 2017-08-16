package se.barsk.park.main_ui

import android.graphics.Rect
import android.support.annotation.DrawableRes
import android.support.v7.widget.RecyclerView
import android.view.View


/**
 * Decoration with alternating colors.
 */
data class AlternatingColorItemDecoration(@DrawableRes val oddBackground: Int, @DrawableRes val evenBackground: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view)
        view.setBackgroundResource(if (position % 2 == 0) evenBackground else oddBackground)
    }
}