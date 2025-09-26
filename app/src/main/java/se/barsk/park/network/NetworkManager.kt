package se.barsk.park.network

import android.content.Context
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.json.FuelJson
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.getAs
import org.json.JSONArray
import org.json.JSONObject
import se.barsk.park.BuildConfig
import se.barsk.park.ParkApp
import se.barsk.park.R
import se.barsk.park.datatypes.OwnCar
import se.barsk.park.datatypes.ParkedCar

/**
 * The network manager is used to make requests to the parking server.
 */
open class NetworkManager {

    companion object {
        private const val STATUS = "status"
        private const val PARK = "park"
        private const val UNPARK = "unpark"
    }

    private val userAgent = "Opark/${BuildConfig.VERSION_NAME}"

    private enum class Action { PARK, UNPARK }

    @Suppress("MemberVisibilityCanBePrivate") // Used from subclass in mock flavor
    protected fun readServerFromStorage() = ParkApp.storageManager.getServer()

    /**
     * Makes a http request to the park server to check the current status.
     * @param context context to use to get resources for error messages
     * @param resultReadyListener callback function to be called when the result is ready
     */
    open fun checkStatus(context: Context, resultReadyListener: (Result) -> Unit) {
        val serverUrl = readServerFromStorage()
        if (serverUrl.isBlank()) {
            resultReadyListener(Result.NoServer)
            return
        }
        Fuel.get(serverUrl + STATUS)
                .header(mapOf("User-Agent" to userAgent))
                .responseJson { _, _, result ->
                    when (result) {
                        is com.github.kittinunf.result.Result.Failure -> {
                            val (_, fuelError) = result
                            resultReadyListener(Result.Fail(null, context.getString(R.string.failed_to_update_generic, fuelError)))
                        }
                        is com.github.kittinunf.result.Result.Success -> {
                            try {
                                val data: JSONObject = result.getAs<FuelJson>()?.obj() as JSONObject
                                val parkedCars = getParkedCarsFromResponse(data)
                                resultReadyListener(Result.Success(parkedCars))
                            } catch (e: org.json.JSONException) {
                                val errorMessage = context.getString(R.string.failed_to_update) +
                                        "\n" + context.getString(R.string.fail_reason_unknown_data)
                                resultReadyListener(Result.Fail(null, errorMessage))
                            } catch (e: Exception) {
                                val errorMessage = context.getString(R.string.failed_to_update) +
                                        "\n" + context.getString(R.string.fail_reason_unknown_error)
                                resultReadyListener(Result.Fail(null, errorMessage))
                            }
                        }
                    }
                }
    }

    @Suppress("MemberVisibilityCanBePrivate") // Used in mock flavor
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

    private fun doAction(context: Context, ownCar: OwnCar, action: Action, resultReadyListener: (Result) -> Unit) {
        val serverUrl = readServerFromStorage()
        if (serverUrl.isBlank()) {
            return
        }
        val errorMessage: String
        val url: String
        val parameters: MutableList<Pair<String, Any?>> = mutableListOf()
        parameters.add(Pair("regno", ownCar.regNo))
        when (action) {
            Action.PARK -> {
                errorMessage = context.getString(R.string.failed_to_park, ownCar.regNo)
                url = serverUrl + PARK
                parameters.add(Pair("user", ownCar.owner))
            }
            Action.UNPARK -> {
                errorMessage = context.getString(R.string.failed_to_unpark, ownCar.regNo)
                url = serverUrl + UNPARK
            }
        }
        Fuel.post(url, parameters)
                .header(mapOf("User-Agent" to userAgent))
                .responseJson { _, response, result ->
                    when (result) {
                        is com.github.kittinunf.result.Result.Success -> {
                            try {
                                val data: JSONObject = result.getAs<FuelJson>()?.obj() as JSONObject
                                val success = data.getString("result") == "ok"
                                val parkedCars = getParkedCarsFromResponse(data)
                                if (success) {
                                    resultReadyListener(Result.Success(parkedCars))
                                } else {
                                    resultReadyListener(Result.Fail(parkedCars, errorMessage))
                                }
                            } catch (e: org.json.JSONException) {
                                val msg = "$errorMessage\n" +
                                        context.getString(R.string.fail_reason_unknown_data)
                                resultReadyListener(Result.Fail(null, msg))
                            } catch (e: Exception) {
                                val msg = "$errorMessage\n" +
                                        context.getString(R.string.fail_reason_unknown_error)
                                resultReadyListener(Result.Fail(null, msg))
                            }
                        }
                        is com.github.kittinunf.result.Result.Failure -> {
                            val msg = "$errorMessage\n" +
                                    "${response.statusCode}: ${response.responseMessage}"
                            resultReadyListener(Result.Fail(null, msg))
                        }
                    }
                }
    }

    /**
     * Makes an http post request to the park server to park a car.
     * @param context context to use to get resources for error messages
     * @param ownCar The car to park.
     * @param resultReadyListener callback function to be called when the result is ready
     */
    open fun parkCar(context: Context, ownCar: OwnCar, resultReadyListener: (Result) -> Unit) =
            doAction(context, ownCar, Action.PARK, resultReadyListener)

    /**
     * Makes an http post request to the park server to unpark a car.
     * @param context context to use to get resources for error messages
     * @param car The car to unpark.
     * @param resultReadyListener callback function to be called when the result is ready
     */
    open fun unparkCar(context: Context, car: OwnCar, resultReadyListener: (Result) -> Unit) =
            doAction(context, car, Action.UNPARK, resultReadyListener)

}