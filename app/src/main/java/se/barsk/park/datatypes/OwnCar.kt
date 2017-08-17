package se.barsk.park.datatypes

import java.util.*

/**
 * A representation of the users own car
 */
data class OwnCar(var regNo: String, var owner: String, var nickName: String = "car", val id: String = UUID.randomUUID().toString()) : Car(regNo, owner) {
    var parked = false // Not part of hash code / equals
}