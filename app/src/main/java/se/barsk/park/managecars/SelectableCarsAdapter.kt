package se.barsk.park.managecars

import android.util.SparseBooleanArray
import se.barsk.park.datatypes.Car
import se.barsk.park.mainui.CarsAdapter

/**
 * Adapter for a selectable list of cars
 */
class SelectableCarsAdapter(cars: List<Car>, listener: (Car) -> Unit) :
        CarsAdapter(Type.MANAGE_CARS, cars, listener) {

    var selectedItemsIds = SparseBooleanArray()

    fun toggleSelection(position: Int) {
        if (isSelected(position)) {
            selectedItemsIds.delete(position)
        } else {
            selectedItemsIds.put(position, true)
        }

        updateViewLayout(position)
    }

    fun selectAll() {
        for (i in 0 until itemCount) {
            selectedItemsIds.put(i, true)
            updateViewLayout(i)
        }
    }

    fun clearSelection() {
        selectedItemsIds.clear()
        for (i in 0 until itemCount) {
            updateViewLayout(i)
        }
    }

    /**
     * Updates the layout of the item located at the given position based on the selection state
     */
    private fun updateViewLayout(position: Int) {
        val entry = recyclerView?.findViewHolderForAdapterPosition(position)?.itemView
        if (entry != null) {
            entry as ManageCarsListEntry
            if (isSelected(position)) {
                entry.select()
            } else {
                entry.deselect()
            }
        }
    }

    fun hasSelectedItems(): Boolean = selectedItemsIds.size() > 0
    fun numSelectedItems(): Int = selectedItemsIds.size()
    override fun isSelected(position: Int): Boolean = selectedItemsIds[position, false]
}