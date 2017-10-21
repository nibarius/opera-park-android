package se.barsk.park.network

import android.content.Context
import android.os.Handler
import org.joda.time.DateTime
import se.barsk.park.BuildConfig
import se.barsk.park.datatypes.OwnCar
import se.barsk.park.datatypes.ParkedCar

/**
 * A mock implementation of the network manager that does not talk to the network
 * and instead uses hard coded well defined content instead.
 */
class MockNetworkManager(private val initialParkedCars: Int = BuildConfig.initialParkedCars) : NetworkManager() {

    // Let the parked cars be empty until the first time checkStatus is called.
    private var parkedCars: MutableList<ParkedCar> = mutableListOf()
    private val networkDelay: Long = 300

    override fun readServerFromStorage() = "http://park.example.com"

    override fun checkStatus(context: Context, resultReadyListener: (Result) -> Unit) {
        updateState = UpdateState.UPDATE_IN_PROGRESS
        if (parkedCars.isEmpty()) {
            val initial = listOf(
                        ParkedCar("DZU 241", "Henrik", "2017-10-01 08:05:15"),
                        ParkedCar("CRF 461", "Erik", "2017-10-01 08:16:55"),
                        ParkedCar("WNF 766", "Rikard", "2017-10-01 08:21:06"),
                        ParkedCar("AGF 487", "Niklas", "2017-10-01 08:29:53"),
                        ParkedCar("ALP 110", "Margaretha", "2017-10-29 08:38:14"),
                        ParkedCar("MLB 942", "Per", "2017-10-01 09:01:33"))
            parkedCars = initial.subList(0, initialParkedCars).toMutableList()
        }
        Handler().postDelayed({ resultReady(resultReadyListener) }, networkDelay)
    }

    private fun resultReady(resultReadyListener: (Result) -> Unit) {
        updateState = UpdateState.IDLE
        state = State.HAVE_MADE_SUCCESSFUL_REQUEST
        resultReadyListener(Result.Success(parkedCars))
    }

    override fun parkCar(context: Context, ownCar: OwnCar, resultReadyListener: (Result) -> Unit) {
        Handler().postDelayed({ addIfNotExist(ownCar, resultReadyListener) }, networkDelay)
    }

    override fun unparkCar(context: Context, car: OwnCar, resultReadyListener: (Result) -> Unit) {
        Handler().postDelayed({ removeIfExists(car, resultReadyListener) }, networkDelay)
    }

    private fun addIfNotExist(ownCar: OwnCar, resultReadyListener: (Result) -> Unit) {
        parkedCars
                .filter { it.regNo == ownCar.regNo }
                .forEach {
                    resultReadyListener(Result.Success(parkedCars))
                    return
                }
        parkedCars.add(ParkedCar(ownCar.regNo, ownCar.owner, DateTime.now().toString("yyyy-MM-dd HH:mm:ss")))
        resultReadyListener(Result.Success(parkedCars))
    }

    private fun removeIfExists(ownCar: OwnCar, resultReadyListener: (Result) -> Unit) {
        (parkedCars.size - 1 downTo 0)
                .filter { parkedCars[it].regNo == ownCar.regNo }
                .forEach { parkedCars.removeAt(it) }
        resultReadyListener(Result.Success(parkedCars))
    }
}