package se.barsk.park.datatypes

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _parkedCars = MutableStateFlow(initialParkedCars)
    val parkedCarsFlow = _parkedCars.asStateFlow()

    private var listeners: MutableList<GarageStatusChangedListener> = mutableListOf()

    val parkedCars: List<ParkedCar>
        get() = _parkedCars.value

    val spotsFree: Int
        get() = CAPACITY - parkedCars.count()

    fun addListener(listener: GarageStatusChangedListener) = listeners.add(listener)
    fun clear() {
        _parkedCars.value = listOf()
        notifyLegacyListeners()
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

    private fun notifyLegacyListeners() {
        for (listener in listeners) {
            listener.onGarageStatusChange()
        }
    }

    private fun onResultReady(result: Result) = when (result) {
        is Result.Success -> {
            notifyListenersAboutReady()
            _parkedCars.value = result.parkedCars.toList()
            notifyLegacyListeners()
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