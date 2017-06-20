package se.barsk.park

/**
 * A representation of the users own car
 */
data class OwnCar(val regNo: String, val owner: String, val nickName: String = "car") : Car(regNo, owner)