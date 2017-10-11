package se.barsk.park.datatypes

/**
 * A mock implementation of the CarCollection with a hard coded list of cars to
 * get well defined data and stable data. It does not persist any changes to storage.
 */
class MockCarCollection : CarCollection() {
    override fun readCarsFromStorage(): MutableList<OwnCar> {
        val car1 = OwnCar("ALP 110", "Margaretha")
        val car2 = OwnCar("MLB 803", "Margaretha")
        return mutableListOf(car1, car2)
    }

    override fun persistRemoval(ownCar: OwnCar) = Unit
    override fun persistUpdate(ownCar: OwnCar, position: Int) = Unit
}