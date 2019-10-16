package se.barsk.park.managecars

import androidx.recyclerview.widget.RecyclerView
import android.util.SparseBooleanArray
import se.barsk.park.datatypes.Car
import se.barsk.park.datatypes.OwnCar
import se.barsk.park.mainui.CarsAdapter

/**
 * Adapter for a selectable list of own cars
 */
class SelectableCarsAdapter(cars: List<OwnCar>, listener: (Car) -> Unit) :
        CarsAdapter(Type.MANAGE_CARS, cars, listener) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
            (holder.itemView as ManageCarsListEntry)
                    .showItem(cars[position], selected = isSelected(position))

    var selectedItemsIds = SparseBooleanArray()
    @Suppress("UNCHECKED_CAST")
    var ownCars: List<OwnCar>
        get() = cars as List<OwnCar>
        set(value) {
            cars = value
        }

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

    @Suppress("MemberVisibilityCanBePrivate") // Used in mock flavor
    fun isSelected(position: Int): Boolean = selectedItemsIds[position, false]
}