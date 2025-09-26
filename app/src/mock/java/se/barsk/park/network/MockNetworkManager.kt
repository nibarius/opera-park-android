package se.barsk.park.network

import android.content.Context
import android.os.Handler
import android.os.Looper
import org.joda.time.DateTime
import se.barsk.park.BuildConfig
import se.barsk.park.R
import se.barsk.park.datatypes.OwnCar
import se.barsk.park.datatypes.ParkedCar

/**
 * A mock implementation of the network manager that does not talk to the network
 * and instead uses hard coded well defined content instead.
 */
class MockNetworkManager(private val initialParkedCars: Int = BuildConfig.initialParkedCars,
                         private val parkedCars: MutableList<ParkedCar> =
                                 defaultParkedCars(initialParkedCars)) : NetworkManager() {

    companion object {
        private fun defaultParkedCars(cars: Int): MutableList<ParkedCar> {
            return listOf(
                    ParkedCar("DZU 241", "Henrik", "2017-10-01 08:05:15"),
                    ParkedCar("CRF 461", "Erik", "2017-10-01 08:16:55"),
                    ParkedCar("WNF 766", "Rikard", "2017-10-01 08:21:06"),
                    ParkedCar("AGF 487", "Niklas", "2017-10-01 08:29:53"),
                    ParkedCar("MLB 942", "Per", "2017-10-01 09:01:33"),
                    ParkedCar("MLB 84A", "Peter", "2017-10-29 08:38:14"))
                    .subList(0, cars).toMutableList()
        }
    }

    private val networkDelay: Long = 300

    var hasConnection = true

    override fun checkStatus(context: Context, resultReadyListener: (Result) -> Unit) {
        if (readServerFromStorage().isBlank()) {
            resultReadyListener(Result.NoServer)
            return
        }
        Handler(Looper.getMainLooper()).postDelayed({ resultReady(resultReadyListener) }, networkDelay)
    }

    private fun resultReady(resultReadyListener: (Result) -> Unit) {
        if (hasConnection) {
            resultReadyListener(Result.Success(parkedCars))
        } else {
            resultReadyListener(Result.Fail(null, ""))
        }
    }

    override fun parkCar(context: Context, ownCar: OwnCar, resultReadyListener: (Result) -> Unit) {
        if (readServerFromStorage().isBlank()) {
            return
        }
        Handler(Looper.getMainLooper()).postDelayed({ addIfNotExist(context, ownCar, resultReadyListener) }, networkDelay)
    }

    override fun unparkCar(context: Context, car: OwnCar, resultReadyListener: (Result) -> Unit) {
        if (readServerFromStorage().isBlank()) {
            return
        }
        Handler(Looper.getMainLooper()).postDelayed({ removeIfExists(context, car, resultReadyListener) }, networkDelay)
    }

    // Add a car to the parked cars list if it does not already exist, if it does signal a fail
    // like the real server does.
    private fun addIfNotExist(context: Context,
                              ownCar: OwnCar,
                              resultReadyListener: (Result) -> Unit) {
        if (!hasConnection) {
            return
        }
        if (parkedCars.any { it.regNo == ownCar.regNo }) {
            resultReadyListener(Result.Fail(parkedCars, context.getString(R.string.failed_to_park, ownCar.regNo)))
        } else {
            parkedCars.add(ParkedCar(ownCar.regNo, ownCar.owner, DateTime.now().toString("yyyy-MM-dd HH:mm:ss")))
            resultReadyListener(Result.Success(parkedCars))
        }

    }

    // Remove a car from the parked cars list if it exist, if it doesn't signal a fail
    // like the real server does.
    private fun removeIfExists(context: Context,
                               ownCar: OwnCar,
                               resultReadyListener: (Result) -> Unit) {
        if (!hasConnection) {
            return
        }
        (parkedCars.size - 1 downTo 0)
                .filter { parkedCars[it].regNo == ownCar.regNo }
                .let { carsToRemove ->
                    if (carsToRemove.isEmpty()) {
                        resultReadyListener(Result.Fail(parkedCars, context.getString(R.string.failed_to_unpark, ownCar.regNo)))
                    } else {
                        carsToRemove.forEach { parkedCars.removeAt(it) }
                        resultReadyListener(Result.Success(parkedCars))
                    }
                }
    }
}