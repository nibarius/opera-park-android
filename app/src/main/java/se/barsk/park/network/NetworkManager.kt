package se.barsk.park.network

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.core.Json
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.result.getAs
import org.json.JSONArray
import org.json.JSONObject
import se.barsk.park.BuildConfig
import se.barsk.park.datatypes.OwnCar
import se.barsk.park.datatypes.ParkedCar

/**
 * The network manager is used to make requests to the parking server.
 */
object NetworkManager {
    private val BASE_URL = BuildConfig.parkServer
    private const val STATUS = "status"
    private const val PARK = "park"
    private const val UNPARK = "unpark"

    private enum class Action {PARK, UNPARK }

    /**
     * Makes a http request to the park server to check the current status.
     * @param resultReadyListener callback function to be called when the result is ready
     */
    fun checkStatus(resultReadyListener: (Result) -> Unit) {
        Fuel.get(BASE_URL + STATUS).responseJson { _, _, result ->
            when (result) {
                is com.github.kittinunf.result.Result.Failure -> {
                    val (_, error) = result
                    resultReadyListener(Result.Fail(null, "Failed to get parking status: $error"))
                }
                is com.github.kittinunf.result.Result.Success -> {
                    try {
                        val data: JSONObject = result.getAs<Json>()?.obj() as JSONObject
                        val parkedCars = getParkedCarsFromResponse(data)
                        resultReadyListener(Result.Success(parkedCars))
                    }
                    catch (e: org.json.JSONException) {
                        resultReadyListener(Result.Fail(null, "Failed to get parking status\nUnknown data returned by server"))
                    }
                    catch (e: Exception) {
                        resultReadyListener(Result.Fail(null, "Failed to get parking status\nUnknown error"))
                    }
                }
            }
        }
    }

    internal fun getParkedCarsFromResponse(data: JSONObject): List<ParkedCar> {
        val usedSpots: JSONArray = data.getJSONArray("used")
        val parkedCars: MutableList<ParkedCar> = mutableListOf()
        for (index in 0..usedSpots.length() - 1) {
            val regNo: String = usedSpots.getJSONObject(index).getString("regno")
            val owner: String = usedSpots.getJSONObject(index).getString("user")
            val startTime: String = usedSpots.getJSONObject(index).getString("start")
            parkedCars.add(ParkedCar(regNo, owner, startTime))
        }
        return parkedCars
    }

    private fun doAction(ownCar: OwnCar, action: Action, resultReadyListener: (Result) -> Unit) {
        val errorMessage: String
        val url: String
        val parameters: MutableList<Pair<String, Any?>> = mutableListOf()
        parameters.add(Pair("regno", ownCar.regNo))
        when (action) {
            Action.PARK -> {
                errorMessage = "Failed to park " + ownCar.regNo
                url = BASE_URL + PARK
                parameters.add(Pair("user", ownCar.owner))
            }
            Action.UNPARK -> {
                errorMessage = "Failed to unpark " + ownCar.regNo
                url = BASE_URL + UNPARK
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
                            }
                            catch (e: org.json.JSONException) {
                                val msg = "$errorMessage\n" + "Unknown data returned by server"
                                resultReadyListener(Result.Fail(null, msg))
                            }
                            catch (e: Exception) {
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