package se.barsk.park.datatypes

/**
 * A mock implementation of the CarCollection with a hard coded list of cars to
 * get well defined data and stable data. It does not persist any changes to storage.
 */
class MockCarCollection(private var numCars: Int = 2) : CarCollection() {
    override fun readCarsFromStorage() {
        val car1 = OwnCar("ALP 110", "Margaretha")
        val car2 = OwnCar("MLB 803", "Margaretha")
        _ownCars.value = listOf(car1, car2)
    }

    //only to be used from tests
    fun reset() {
        readCarsFromStorage()
    }

    override suspend fun persistRemoval(ownCar: OwnCar) {
        // Noop for mock
    }
    override suspend fun persistUpdate(ownCar: OwnCar, position: Int) {
        // Noop for mock
    }

    /**
     * Method intended to only be run by unit tests. Replaces the entire content
     * of the car collection with the given list of cars. Used to bypass persistent
     * storage
     */
    fun replaceContent(newCars: MutableList<OwnCar>) {
        _ownCars.value = newCars
    }

    /**
     * Returns the car at the given position.
     */
    fun getCarAtPosition(position: Int): OwnCar = _ownCars.value[position]
}