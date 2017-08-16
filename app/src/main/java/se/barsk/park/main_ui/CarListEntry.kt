package se.barsk.park.main_ui

import se.barsk.park.datatypes.Car

/**
 * An interface for all lists of cars (parked cars and own cars)
 */
interface CarListEntry {
    fun showItem(car: Car, selected: Boolean)
}