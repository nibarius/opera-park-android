package se.barsk.park.carcollection

import se.barsk.park.datatypes.CarCollection
import se.barsk.park.datatypes.OwnCar

/**
 * A mock implementation of the CarCollection with a hard coded list of cars to
 * get well defined data and stable data. It does not persist any changes to storage.
 */
class MockCarCollection(var numCars: Int = 2) : CarCollection() {
    override fun readCarsFromStorage(): MutableList<OwnCar> {
        val car1 = OwnCar("ALP 110", "Margaretha")
        val car2 = OwnCar("MLB 803", "Margaretha")
        return listOf(car1, car2).subList(0, numCars).toMutableList()
    }

    override fun persistRemoval(ownCar: OwnCar) = Unit
    override fun persistUpdate(ownCar: OwnCar, position: Int) = Unit

    /**
     * Method intended to only be run by unit tests. Replaces the entire content
     * of the car collection with the given list of cars. Used to bypass persistent
     * storage
     */
    fun replaceContent(newCars: MutableList<OwnCar>) {
        ownCars.clear()
        ownCars.addAll(newCars)
        notifyListeners()
    }
}