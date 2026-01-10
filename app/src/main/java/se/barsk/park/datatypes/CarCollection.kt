package se.barsk.park.datatypes

import android.util.SparseBooleanArray
import androidx.core.util.size
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import se.barsk.park.ParkApp

/**
 * A collection of all the cars the user owns. On initialization it reads
 * cars from local storage and any changes done are saved to local storage
 * as they happen.
 */
open class CarCollection {

    protected val _ownCars = MutableStateFlow<List<OwnCar>>(emptyList())
    val ownCarsFlow = _ownCars.asStateFlow()

    init {
        // Start collecting the flow as soon as the class is instantiated
        readCarsFromStorage()
    }

    protected open fun readCarsFromStorage() {
        ParkApp.storageManager.fetchAllCars()
            .onEach { updatedCars ->
                _ownCars.value = updatedCars
            }.launchIn(ParkApp.applicationScope)
    }


    /**
     * Update the parking status for all the cars in the car collection
     * @param garage The Garage to check the parking status against
     * @return True if the parking status was changed for any of the cars in the collection
     */
    fun updateParkStatus(garage: Garage): Boolean {
        var anyChange = false
        val currentCars = _ownCars.value.toMutableList()
        for (car in currentCars) {
            val newValue = garage.isParked(car)
            if (newValue != car.parked) {
                car.parked = newValue
                anyChange = true
            }
        }

        if (anyChange) {
            _ownCars.value = currentCars
        }
        return anyChange
    }

    /**
     * Adds a new car to the car collection and persists it to persistent storage
     */
    suspend fun addCar(ownCar: OwnCar) {
        val updatedList = _ownCars.value + ownCar
        _ownCars.value = updatedList
        persistUpdate(ownCar, updatedList.lastIndex)
    }

    /**
     * Removes the car at the given index and persists the removal to persistent storage
     */
    private suspend fun removeCarAt(index: Int) {
        val carToRemove = _ownCars.value[index]
        _ownCars.value = _ownCars.value.toMutableList().apply { removeAt(index) }
        persistRemoval(carToRemove)
    }

    /**
     * Remove all cars at the positions indicated by the given sparse boolean array.
     */
    suspend fun removeCars(which: SparseBooleanArray) {
        for (index in which.size - 1 downTo 0) {
            removeCarAt(which.keyAt(index))
        }
    }

    /**
     * Remove all cars in the given collection and persists the removal to persistent storage
     */
    suspend fun removeCars(cars: Collection<OwnCar>) {
        for (car in cars) {
            _ownCars.value = _ownCars.value.filterNot { it.id == car.id }
            persistRemoval(car)
        }
    }

    /**
     * Updates a car and persists the change to persistent storage
     */
    suspend fun updateCar(ownCar: OwnCar) {
        val position = positionOf(ownCar)
        _ownCars.value = _ownCars.value.toMutableList().apply { set(position, ownCar) }
        persistUpdate(ownCar, position)
    }

    open suspend fun persistRemoval(ownCar: OwnCar) = ParkApp.storageManager.remove(ownCar)
    open suspend fun persistUpdate(ownCar: OwnCar, position: Int) = ParkApp.storageManager.insertOrReplace(ownCar, position)

    @Suppress("MemberVisibilityCanBePrivate") // Used by mock flavor
    fun positionOf(car: OwnCar): Int = positionOf(car.id)

    private fun positionOf(carId: String): Int {
        val position = _ownCars.value.indexOfFirst { it.id == carId }
        if (position == -1) {
            throw RuntimeException("A car with id $carId does not exist in the CarCollection")
        }
        return position
    }

    /**
     * Returns a list of all the cars in the collection.
     */
    fun getCars(): List<OwnCar> = _ownCars.value.toList()

}