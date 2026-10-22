package se.barsk.park.network

import android.content.Context
import android.os.Handler
import android.os.Looper
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
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

    private val client = OkHttpClient()
    private val userAgent = "Opark/${BuildConfig.VERSION_NAME}"
    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

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
        val request = Request.Builder()
            .url(serverUrl + STATUS)
            .header("User-Agent", userAgent)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mainHandler.post {
                    resultReadyListener(Result.Fail(null, context.getString(R.string.failed_to_update_generic, e.localizedMessage ?: e.toString())))
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val bodyString = response.body?.string() ?: ""
                    if (!response.isSuccessful) {
                        mainHandler.post {
                            resultReadyListener(Result.Fail(null, context.getString(R.string.failed_to_update_generic, "${response.code}: ${response.message}")))
                        }
                        return
                    }
                    val data = JSONObject(bodyString)
                    val parkedCars = getParkedCarsFromResponse(data)
                    mainHandler.post {
                        resultReadyListener(Result.Success(parkedCars))
                    }
                } catch (e: org.json.JSONException) {
                    val errorMessage = context.getString(R.string.failed_to_update) +
                            "\n" + context.getString(R.string.fail_reason_unknown_data)
                    mainHandler.post {
                        resultReadyListener(Result.Fail(null, errorMessage))
                    }
                } catch (e: Exception) {
                    val errorMessage = context.getString(R.string.failed_to_update) +
                            "\n" + context.getString(R.string.fail_reason_unknown_error)
                    mainHandler.post {
                        resultReadyListener(Result.Fail(null, errorMessage))
                    }
                }
            }
        })
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
        val formBuilder = FormBody.Builder().add("regno", ownCar.regNo)
        when (action) {
            Action.PARK -> {
                errorMessage = context.getString(R.string.failed_to_park, ownCar.regNo)
                url = serverUrl + PARK
                formBuilder.add("user", ownCar.owner)
            }
            Action.UNPARK -> {
                errorMessage = context.getString(R.string.failed_to_unpark, ownCar.regNo)
                url = serverUrl + UNPARK
            }
        }
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", userAgent)
            .post(formBuilder.build())
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val msg = "$errorMessage\n${e.localizedMessage ?: e.toString()}"
                mainHandler.post {
                    resultReadyListener(Result.Fail(null, msg))
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val bodyString = response.body?.string() ?: ""
                    if (!response.isSuccessful) {
                        val msg = "$errorMessage\n${response.code}: ${response.message}"
                        mainHandler.post {
                            resultReadyListener(Result.Fail(null, msg))
                        }
                        return
                    }
                    val data = JSONObject(bodyString)
                    val success = data.optString("result") == "ok" || data.getString("result") == "ok"
                    val parkedCars = getParkedCarsFromResponse(data)
                    mainHandler.post {
                        if (success) {
                            resultReadyListener(Result.Success(parkedCars))
                        } else {
                            resultReadyListener(Result.Fail(parkedCars, errorMessage))
                        }
                    }
                } catch (e: org.json.JSONException) {
                    val msg = "$errorMessage\n" +
                            context.getString(R.string.fail_reason_unknown_data)
                    mainHandler.post {
                        resultReadyListener(Result.Fail(null, msg))
                    }
                } catch (e: Exception) {
                    val msg = "$errorMessage\n" +
                            context.getString(R.string.fail_reason_unknown_error)
                    mainHandler.post {
                        resultReadyListener(Result.Fail(null, msg))
                    }
                }
            }
        })
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
