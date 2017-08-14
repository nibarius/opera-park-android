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

    var selectedItemsIds = SparseBooleanArray()

    fun toggleSelection(position: Int) {
        if (selectedItemsIds[position, false]) {
            selectedItemsIds.delete(position)
        } else {
            selectedItemsIds.put(position, true)
        }

        notifyItemChanged(position)
    }

    fun selectAll() {
        for (i in 0 .. itemCount) {
            selectedItemsIds.put(i, true)
        }
    }

    fun clearSelection() {
        selectedItemsIds.clear()
        notifyDataSetChanged()
    }

    fun hasSelectedItems(): Boolean = selectedItemsIds.size() > 0
    fun numSelectedItems(): Int = selectedItemsIds.size()
    override fun isSelected(position: Int): Boolean = selectedItemsIds[position, false]
}