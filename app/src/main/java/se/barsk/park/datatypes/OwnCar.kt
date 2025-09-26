package se.barsk.park.datatypes

import java.util.UUID

/**
 * A representation of the users own car
 */
data class OwnCar(override val regNo: String, override val owner: String, val nickName: String = "car", val id: String = UUID.randomUUID().toString()) : Car() {
    var parked = false // Not part of hash code / equals

    /**
     * Returns true if the given car is the same (real world car) as this one,
     * that is if their license plate match.
     */
    fun isSameCar(otherCar: OwnCar) = regNo == otherCar.regNo
}