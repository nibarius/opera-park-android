package se.barsk.park.datatypes

/**
 * A mock implementation of the CarCollection with a hard coded list of cars to
 * get well defined data and stable data. It does not persist any changes to storage.
 */
class MockCarCollection(private var numCars: Int = 2) : CarCollection() {
    override fun readCarsFromStorage() {
        val car1 = OwnCar("ALP 110", "Margaretha")
        val car2 = OwnCar("MLB 803", "Margaretha")
        ownCars = mutableListOf(car1, car2)
        _ownCars.value = ownCars.toList()
    }

    override suspend fun persistRemoval(ownCar: OwnCar) {
        // The ones calling this method updates ownCars before persisting the change
        _ownCars.value = ownCars.toList()
    }
    override suspend fun persistUpdate(ownCar: OwnCar, position: Int) {
        // The ones calling this method updates ownCars before persisting the change
        _ownCars.value = ownCars.toList()
    }

    /**
     * Method intended to only be run by unit tests. Replaces the entire content
     * of the car collection with the given list of cars. Used to bypass persistent
     * storage
     */
    fun replaceContent(newCars: MutableList<OwnCar>) {
        ownCars.clear()
        ownCars.addAll(newCars)
        _ownCars.value = ownCars.toList()
        notifyListeners()
    }

    /**
     * Returns the car at the given position.
     */
    fun getCarAtPosition(position: Int): OwnCar = ownCars[position]
}