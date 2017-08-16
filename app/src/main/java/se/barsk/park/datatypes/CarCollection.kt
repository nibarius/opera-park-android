package se.barsk.park.datatypes

/**
 * A collection of all the cars the user owns.
 */
class CarCollection(val ownCars: MutableList<OwnCar> = mutableListOf()) {
    /**
     * Update the parking status for all the cars in the car collection
     * @param garage The Garage to check the parking status against
     * @return True if the parking status was changed for any of the cars in the collection
     */
    fun updateParkStatus(garage: Garage): Boolean {
        var anyChange = false
        for (car in ownCars) {
            val newValue = garage.isParked(car)
            if (newValue != car.parked) {
                car.parked = newValue
                anyChange = true
            }
        }
        return anyChange
    }
}