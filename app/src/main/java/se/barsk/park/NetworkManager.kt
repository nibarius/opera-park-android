package se.barsk.park

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.core.Json
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getAs
import org.json.JSONArray
import org.json.JSONObject

/**
 * The network manager is used to make requests to the parking server.
 */
object NetworkManager {
    fun checkStatus(): Pair<Int, List<ParkedCar>> {
        var parkedCars: Pair<Int, List<ParkedCar>>
        parkedCars = Pair(6, listOf())
        Fuel.get("http://park.opera.software/status").responseJson { request, response, result ->
            when (result) {
                is Result.Failure -> {
                    print("Error")
                    //Todo: make a real error message
                    val (_, error) = result
                    print(error)
                }
                is Result.Success -> {
                    val data: JSONObject? = result.getAs<Json>()?.obj()
                    if (data != null) {
                        parkedCars = parseData(data)
                    }
                }
            }
        }
        return parkedCars
    }

    internal fun parseData(data: JSONObject): Pair<Int, List<ParkedCar>> {
        val freeSpots = data.getInt("free")
        val usedSpots: JSONArray = data.getJSONArray("used")
        val parkedCars: MutableList<ParkedCar> = mutableListOf()
        for (index in 0..usedSpots.length() - 1) {
            val regNo: String = usedSpots.getJSONObject(index).getString("regno")
            val owner: String = usedSpots.getJSONObject(index).getString("user")
            val startTime: String = usedSpots.getJSONObject(index).getString("start")
            parkedCars.add(ParkedCar(regNo, owner, startTime))
        }
        return Pair(freeSpots, parkedCars)
    }
}