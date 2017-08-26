package se.barsk.park.datatypes

import java.util.*

/**
 * A representation of the users own car
 */
data class OwnCar(override val regNo: String, override val owner: String, val nickName: String = "car", val id: String = UUID.randomUUID().toString()) : Car() {
    var parked = false // Not part of hash code / equals
}