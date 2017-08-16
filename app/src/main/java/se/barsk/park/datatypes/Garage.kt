package se.barsk.park.datatypes

import se.barsk.park.network.NetworkManager
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

    var parkedCars: List<ParkedCar> by Delegates.observable(initialParkedCars) {
        property, oldValue, newValue ->
        for (listener in listeners) {
            listener.onGarageStatusChange()
        }
    }

    val spotsFree: Int
        get() = CAPACITY - parkedCars.count()

    fun addListener(listener: GarageStatusChangedListener) = listeners.add(listener)
    fun isParked(car: OwnCar): Boolean = parkedCars.any { it.regNo == car.regNo }
    fun isFull(): Boolean = parkedCars.size == CAPACITY
    fun updateStatus() = NetworkManager.checkStatus(this::onResultReady)
    fun parkCar(car: OwnCar) = NetworkManager.parkCar(car, this::onResultReady)
    fun unparkCar(car: OwnCar) = NetworkManager.unparkCar(car, this::onResultReady)

    private fun notifyListenersAboutFail(msg: String) {
        for (listener in listeners) {
            listener.onGarageUpdateFail(msg)
        }
    }

    private fun onResultReady(result: Result) {
        when (result) {
            is Result.Success -> {
                parkedCars = result.parkedCars
            }
            is Result.Fail -> {
                notifyListenersAboutFail(result.message)
            }
        }
    }
}