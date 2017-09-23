package se.barsk.park.network

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.core.Json
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.result.getAs
import org.json.JSONArray
import org.json.JSONObject
import se.barsk.park.datatypes.OwnCar
import se.barsk.park.datatypes.ParkedCar
import se.barsk.park.isTesting
import se.barsk.park.storage.StorageManager

/**
 * The network manager is used to make requests to the parking server.
 */
object NetworkManager {
    private var serverUrl: String = if (isTesting()) "" else StorageManager.getServer()
    var state: State = State.FIRST_RESPONSE_NOT_RECEIVED
    private const val STATUS = "status"
    private const val PARK = "park"
    private const val UNPARK = "unpark"

    private enum class Action { PARK, UNPARK }

    /**
     * Describes the state of the NetworkManager. This can be used to decide which
     * placeholder should shown to the user if there are no data from the server available.
     */
    enum class State {
        FIRST_RESPONSE_NOT_RECEIVED,
        ONLY_FAILED_REQUESTS,
        HAVE_MADE_SUCCESSFUL_REQUEST
    }

    fun setServer(server: String) {
        serverUrl = server
        state = State.FIRST_RESPONSE_NOT_RECEIVED
    }

    /**
     * Makes a http request to the park server to check the current status.
     * @param resultReadyListener callback function to be called when the result is ready
     */
    fun checkStatus(resultReadyListener: (Result) -> Unit) {
        if (serverUrl.isEmpty()) {
            resultReadyListener(Result.NoServer())
            return
        }
        Fuel.get(serverUrl + STATUS).responseJson { _, _, result ->
            when (result) {
                is com.github.kittinunf.result.Result.Failure -> {
                    val (_, error) = result
                    if (state == State.FIRST_RESPONSE_NOT_RECEIVED) {
                        state = State.ONLY_FAILED_REQUESTS
                    }
                    resultReadyListener(Result.Fail(null, "Failed to update parking status: $error"))
                }
                is com.github.kittinunf.result.Result.Success -> {
                    try {
                        val data: JSONObject = result.getAs<Json>()?.obj() as JSONObject
                        val parkedCars = getParkedCarsFromResponse(data)
                        state = State.HAVE_MADE_SUCCESSFUL_REQUEST

                        resultReadyListener(Result.Success(parkedCars))
                    } catch (e: org.json.JSONException) {
                        resultReadyListener(Result.Fail(null, "Failed to update parking status\nUnknown data returned by server"))
                    } catch (e: Exception) {
                        resultReadyListener(Result.Fail(null, "Failed to update parking status\nUnknown error"))
                    }
                }
            }
        }
    }

    internal fun getParkedCarsFromResponse(data: JSONObject): List<ParkedCar> {
        val usedSpots: JSONArray = data.getJSONArray("used")
        val parkedCars: MutableList<ParkedCar> = mutableListOf()
        for (index in 0 until usedSpots.length()) {
            val regNo: String = usedSpots.getJSONObject(index).getString("regno")
            val owner: String = usedSpots.getJSONObject(index).getString("user")
            val startTime: String = usedSpots.getJSONObject(index).getString("start")
            parkedCars.add(ParkedCar(regNo, owner, startTime))
        }
        return parkedCars
    }

    private fun doAction(ownCar: OwnCar, action: Action, resultReadyListener: (Result) -> Unit) {
        if (serverUrl.isEmpty()) {
            return
        }
        val errorMessage: String
        val url: String
        val parameters: MutableList<Pair<String, Any?>> = mutableListOf()
        parameters.add(Pair("regno", ownCar.regNo))
        when (action) {
            Action.PARK -> {
                errorMessage = "Failed to park " + ownCar.regNo
                url = serverUrl + PARK
                parameters.add(Pair("user", ownCar.owner))
            }
            Action.UNPARK -> {
                errorMessage = "Failed to unpark " + ownCar.regNo
                url = serverUrl + UNPARK
            }
        }
        Fuel.post(url, parameters)
                .responseJson { request, response, result ->
                    when (result) {
                        is com.github.kittinunf.result.Result.Success -> {
                            try {
                                val data: JSONObject = result.getAs<Json>()?.obj() as JSONObject
                                val success = data.getString("result") == "ok"
                                val parkedCars = getParkedCarsFromResponse(data)
                                if (success) {
                                    resultReadyListener(Result.Success(parkedCars))
                                } else {
                                    resultReadyListener(Result.Fail(parkedCars, errorMessage))
                                }
                            } catch (e: org.json.JSONException) {
                                val msg = "$errorMessage\n" + "Unknown data returned by server"
                                resultReadyListener(Result.Fail(null, msg))
                            } catch (e: Exception) {
                                val msg = "$errorMessage\n" + "Unknown error"
                                resultReadyListener(Result.Fail(null, msg))
                            }
                        }
                        is com.github.kittinunf.result.Result.Failure -> {
                            val msg = "$errorMessage\n" +
                                    "${response.httpStatusCode}: ${response.httpResponseMessage}"
                            resultReadyListener(Result.Fail(null, msg))
                        }
                    }
                }
    }

    /**
     * Makes an http post request to the park server to park a car.
     * @param ownCar The car to park.
     * @param resultReadyListener callback function to be called when the result is ready
     */
    fun parkCar(ownCar: OwnCar, resultReadyListener: (Result) -> Unit) {
        doAction(ownCar, Action.PARK, resultReadyListener)
    }

    /**
     * Makes an http post request to the park server to unpark a car.
     * @param car The car to unpark.
     * @param resultReadyListener callback function to be called when the result is ready
     */
    fun unparkCar(car: OwnCar, resultReadyListener: (Result) -> Unit) {
        doAction(car, Action.UNPARK, resultReadyListener)
    }
}