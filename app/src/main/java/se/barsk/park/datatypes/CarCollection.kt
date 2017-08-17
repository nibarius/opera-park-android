package se.barsk.park.datatypes

import se.barsk.park.storage.StorageManager

/**
 * A collection of all the cars the user owns.
 */
class CarCollection(val ownCars: MutableList<OwnCar> = mutableListOf()) {
    /**
     * Update the parking status for all the cars in the car collection
     * @param garage The Garage to check the parking status against
     * @return True if the parking status was changed for any of the cars in the collection
     */
    fun updateParkStatus(garage: Garage): Boolean {
        var anyChange = false
        for (car in ownCars) {
            val newValue = garage.isParked(car)
            if (newValue != car.parked) {
                car.parked = newValue
                anyChange = true
            }
        }
        return anyChange
    }

    /**
     * Adds a new car to the car collection and persists it to persistent storage
     */
    fun addCar(ownCar: OwnCar) {
        ownCars.add(ownCar)
        StorageManager.insertOrReplace(ownCar, ownCars.lastIndex)
    }

    /**
     * Removes the car at the given index and persists the removal to persistent storage
     */
    fun removeCarAt(index: Int) {
        removeCar(ownCars[index])
    }

    /**
     * Removes the given car and persists the removal to persistent storage
     */
    fun removeCar(ownCar: OwnCar) {
        ownCars.remove(ownCar)
        StorageManager.remove(ownCar)
    }

    /**
     * Updates a car and persists the change to persistent storage
     */
    fun updateCar(ownCar: OwnCar) {
        val position = positionOf(ownCar)
        ownCars[position] = ownCar
        StorageManager.insertOrReplace(ownCar, position)
    }

    fun positionOf(car: OwnCar): Int {
        for ((index, ownCar) in ownCars.withIndex()) {
            if (car.id == ownCar.id) {
                return index
            }
        }
        throw RuntimeException("$car does not exist in the CarCollection")
    }
}