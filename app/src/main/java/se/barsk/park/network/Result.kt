package se.barsk.park.network

import se.barsk.park.datatypes.ParkedCar

/**
 * The result of an attempt to communicate with the park server
 */
sealed class Result {
    data class Success(val parkedCars: List<ParkedCar>) : Result()
    data class Fail(val parkedCars: List<ParkedCar>?, val message: String) : Result()
    class NoServer : Result()
}