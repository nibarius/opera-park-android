package se.barsk.park

import android.graphics.Rect
import android.support.annotation.DrawableRes
import android.support.v7.widget.RecyclerView
import android.util.SparseBooleanArray
import android.view.View

/**
 * Adapter for a selectable list of cars
 */
class SelectableCarsAdapter(cars: List<Car>, listener: (Car) -> Unit) :
        CarsAdapter(Type.MANAGE_CARS, cars, listener) {

    val selectionDecorator: RecyclerView.ItemDecoration = SelectionItemDecoration(R.color.colorEvenRow, R.color.colorOddRow)
    var selectedItemsIds = SparseBooleanArray()

    fun toggleSelection(position: Int) {
        if (selectedItemsIds[position, false]) {
            selectedItemsIds.delete(position)
        }
        else {
            selectedItemsIds.put(position, true)
        }
        notifyDataSetChanged()
    }

    fun clearSelection() {
        selectedItemsIds.clear()
        notifyDataSetChanged()
    }
    fun hasSelectedItems(): Boolean = selectedItemsIds.size() > 0
    fun numSelectedItems(): Int = selectedItemsIds.size()
    fun isSelected(position: Int): Boolean = selectedItemsIds[position, false]



    /**
     * Decoration for using different backgrounds on selected items in a recycler view
     */
    inner class SelectionItemDecoration(@DrawableRes val unselectedBackground: Int, @DrawableRes val selectedBackground: Int) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)
            val position = parent.getChildAdapterPosition(view)
            val selected = this@SelectableCarsAdapter.isSelected(position)
            view.setBackgroundResource(if (selected) selectedBackground else unselectedBackground)
        }
    }
}