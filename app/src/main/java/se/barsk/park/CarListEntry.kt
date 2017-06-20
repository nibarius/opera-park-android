package se.barsk.park

/**
 * An interface for all lists of cars (parked cars and own cars)
 */
interface CarListEntry {
    fun showItem(car: Car)
}