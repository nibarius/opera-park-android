package se.barsk.park.datatypes

import se.barsk.park.isTesting
import se.barsk.park.storage.StorageManager

/**
 * A collection of all the cars the user owns. On initialization it reads
 * cars from local storage and any changes done are saved to local storage
 * as they happen.
 */
object CarCollection {

    private var listeners: MutableList<CarCollectionStatusChangedListener> = mutableListOf()
    private val ownCars: MutableList<OwnCar> = StorageManager.fetchAllCars()

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
    fun updateParkStatus(garage: Garage) {
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
    }

    /**
     * Adds a new car to the car collection and persists it to persistent storage
     */
    fun addCar(ownCar: OwnCar, notify: Boolean = true) {
        ownCars.add(ownCar)
        StorageManager.insertOrReplace(ownCar, ownCars.lastIndex)
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
    fun removeCarAt(index: Int) {
        removeCar(ownCars[index])
    }

    /**
     * Removes the given car and persists the removal to persistent storage
     */
    private fun removeCar(ownCar: OwnCar) {
        ownCars.remove(ownCar)
        StorageManager.remove(ownCar)
        notifyListeners()
    }

    /**
     * Updates a car and persists the change to persistent storage
     */
    fun updateCar(ownCar: OwnCar) {
        val position = positionOf(ownCar)
        ownCars[position] = ownCar
        StorageManager.insertOrReplace(ownCar, position)
        notifyListeners()
    }

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

    /**
     * Method inteded to only be run by unit tests. Replaces the entire content
     * of the car collection with the given list of cars. Used to bypass persistent
     * storage
     */
    fun replaceContent(newCars: MutableList<OwnCar>) {
        if (isTesting()) {
            ownCars.clear()
            ownCars.addAll(newCars)
        }
    }
}