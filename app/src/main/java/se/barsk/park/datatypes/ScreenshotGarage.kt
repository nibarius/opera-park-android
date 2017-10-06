package se.barsk.park.datatypes

import android.content.Context
import se.barsk.park.BuildConfig
import se.barsk.park.network.NetworkManager

/**
 * A mock implementation of the Garage with a hard coded list of cars meant
 * for taking consistent screenshots with well defined data.
 */
class ScreenshotGarage : Garage(listOf()) {
    override fun updateStatus(context: Context) {
        NetworkManager.state = NetworkManager.State.HAVE_MADE_SUCCESSFUL_REQUEST
        notifyListenersAboutReady()
        @Suppress("ConstantConditionIf")
        parkedCars = if (BuildConfig.garageFull) {
            listOf(
                    ParkedCar("DZU 241", "Henrik", "2017-10-01 08:05:15"),
                    ParkedCar("CRF 461", "Erik", "2017-10-01 08:16:55"),
                    ParkedCar("WNF 766", "Rikard", "2017-10-01 08:21:06"),
                    ParkedCar("AGF 487", "Niklas", "2017-10-01 08:29:53"),
                    ParkedCar("ALP 110", "Margaretha", "2017-10-29 08:38:14"),
                    ParkedCar("MLB 942", "Per", "2017-10-01 09:01:33"))
        } else {
            listOf(
                    ParkedCar("DZU 241", "Henrik", "2017-10-01 08:05:15"),
                    ParkedCar("CRF 461", "Erik", "2017-10-01 08:16:55"),
                    ParkedCar("WNF 766", "Rikard", "2017-10-01 08:21:06"),
                    ParkedCar("AGF 487", "Niklas", "2017-10-01 08:29:53"))
        }
    }
}