package se.barsk.park.datatypes

import android.content.Context
import se.barsk.park.ParkApp
import se.barsk.park.network.Result
import kotlin.properties.Delegates

/**
 * A garage that can have several cars parked and can notify listeners
 * of changes to the garage.
 */
class Garage(initialParkedCars: List<ParkedCar> = listOf()) {
    companion object {
        const val CAPACITY = 6
    }

    private var listeners: MutableList<GarageStatusChangedListener> = mutableListOf()

    var parkedCars: List<ParkedCar> by Delegates.observable(initialParkedCars) { _, _, _ ->
        for (listener in listeners) {
            listener.onGarageStatusChange()
        }
    }

    val spotsFree: Int
        get() = CAPACITY - parkedCars.count()

    fun addListener(listener: GarageStatusChangedListener) = listeners.add(listener)
    fun clear() {
        parkedCars = listOf()
    }

    fun isParked(car: OwnCar): Boolean = parkedCars.any { it.regNo == car.regNo }
    fun isFull(): Boolean = parkedCars.size == CAPACITY
    fun isEmpty(): Boolean = parkedCars.isEmpty()
    fun updateStatusFromServer(context: Context) = ParkApp.networkManager.checkStatus(context, this::onResultReady)
    fun parkCar(context: Context, car: OwnCar) = ParkApp.networkManager.parkCar(context, car, this::onResultReady)
    fun unparkCar(context: Context, car: OwnCar) = ParkApp.networkManager.unparkCar(context, car, this::onResultReady)

    private fun notifyListenersAboutReady(success: Boolean = true, errorMessage: String? = null) {
        for (listener in listeners) {
            listener.onGarageUpdateReady(success, errorMessage)
        }
    }

    private fun onResultReady(result: Result) = when (result) {
        is Result.Success -> {
            notifyListenersAboutReady()
            parkedCars = result.parkedCars
        }
        is Result.Fail -> {
            notifyListenersAboutReady(false, result.message)
        }
        is Result.NoServer -> {
            notifyListenersAboutReady(false)
        }
        else -> {
            throw RuntimeException("Unexpected result type given to the Garage")
        }
    }
}