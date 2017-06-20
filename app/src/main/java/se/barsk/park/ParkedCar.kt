package se.barsk.park

/**
 * Representation of a car that is currently parked.
 */
data class ParkedCar(val regNo: String, val owner: String, val startTime: String) : Car(regNo, owner)