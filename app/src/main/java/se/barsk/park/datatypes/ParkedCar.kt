package se.barsk.park.datatypes

/**
 * Representation of a car that is currently parked.
 */
data class ParkedCar(override val regNo: String, override val owner: String, val startTime: String) : Car()