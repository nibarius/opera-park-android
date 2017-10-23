package se.barsk.park.datatypes

import android.util.SparseBooleanArray
import se.barsk.park.ParkApp

/**
 * A collection of all the cars the user owns. On initialization it reads
 * cars from local storage and any changes done are saved to local storage
 * as they happen.
 */
open class CarCollection {

    private var listeners: MutableList<CarCollectionStatusChangedListener> = mutableListOf()
    protected val ownCars: MutableList<OwnCar> by lazy { readCarsFromStorage() }

    open protected fun readCarsFromStorage() = ParkApp.storageManager.fetchAllCars()

    private fun notifyListeners() {
        for (listener in listeners) {
            listener.onCarCollectionStatusChange()
        }
    }

    fun addListener(listener: CarCollectionStatusChangedListener) = listeners.add(listener)
    fun removeListener(listener: CarCollectionStatusChangedListener) = listeners.remove(listener)

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
        if (anyChange) {
            notifyListeners()
        }
        return anyChange
    }

    /**
     * Adds a new car to the car collection and persists it to persistent storage
     */
    fun addCar(ownCar: OwnCar, notify: Boolean = true) {
        ownCars.add(ownCar)
        persistUpdate(ownCar, ownCars.lastIndex)
        if (notify) {
            notifyListeners()
        }
    }

    /**
     * Adds all cars in the given list that isn't already present in the car collection
     * and persists it to persistent storage. If there are no cars, nothing is done.
     */
    fun addCarsThatDoesNotExist(carsToAdd: List<OwnCar>) {
        carsToAdd
                .filterNot { hasCar(it) }
                .forEach { addCar(it, false) }
        notifyListeners()
    }

    /**
     * Removes the car at the given index and persists the removal to persistent storage
     */
    private fun removeCarAt(index: Int) {
        val ownCar = ownCars[index]
        ownCars.remove(ownCar)
        persistRemoval(ownCar)
    }

    /**
     * Remove all cars at the positions indicated by the given sparse boolean array.
     */
    fun removeCars(which: SparseBooleanArray) {
        for (index in which.size() - 1 downTo 0) {
            removeCarAt(which.keyAt(index))
        }
        notifyListeners()
    }

    /**
     * Updates a car and persists the change to persistent storage
     */
    fun updateCar(ownCar: OwnCar) {
        val position = positionOf(ownCar)
        ownCars[position] = ownCar
        persistUpdate(ownCar, position)
        notifyListeners()
    }

    open fun persistRemoval(ownCar: OwnCar) = ParkApp.storageManager.remove(ownCar)
    open fun persistUpdate(ownCar: OwnCar, position: Int) = ParkApp.storageManager.insertOrReplace(ownCar, position)

    /**
     * Returns true if the car collection already has a car with the same license plate
     */
    private fun hasCar(otherCar: OwnCar): Boolean = ownCars.any { it.isSameCar(otherCar) }

    fun positionOf(car: OwnCar): Int = positionOf(car.id)

    private fun positionOf(carId: String): Int {
        for ((index, ownCar) in ownCars.withIndex()) {
            if (carId == ownCar.id) {
                return index
            }
        }
        throw RuntimeException("A car with id $carId does not exist in the CarCollection")
    }

    /**
     * Returns the car with the given id
     */
    fun getCar(id: String): OwnCar = ownCars[positionOf(id)]

    /**
     * Returns the id for the car at the given position
     */
    fun getCarId(position: Int): String = ownCars[position].id

    /**
     * Returns a list of all the cars in the collection.
     */
    fun getCars(): List<OwnCar> = ownCars.toList()

    /**
     * Returns the car at the given position.
     */
    fun getCarAtPosition(position: Int): OwnCar = ownCars[position]

}