package se.barsk.park.mainui

enum class ParkingState {
    NO_SERVER,
    WAITING_ON_RESPONSE,
    REQUEST_FAILED,
    EMPTY,
    FREE_SPACE,
    ALMOST_FULL,
    FULL;

    fun showsPlaceholder(): Boolean = ordinal <= EMPTY.ordinal
    fun communicatesWithServer(): Boolean = ordinal >= EMPTY.ordinal
}
